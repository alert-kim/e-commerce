package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.application.order.command.PayOrderProcessorCommand
import kr.hhplus.be.server.domain.balance.exception.BelowMinBalanceAmountException
import kr.hhplus.be.server.domain.balance.exception.InsufficientBalanceException
import kr.hhplus.be.server.domain.common.LockAcquisitionFailException
import kr.hhplus.be.server.domain.order.OrderStatus
import kr.hhplus.be.server.testutil.DatabaseTestHelper
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
class OrderPaymentProcessorConcurrencyTest {

    @Autowired
    private lateinit var processor: OrderPaymentProcessor

    @Autowired
    private lateinit var databaseTestHelper: DatabaseTestHelper

    @Nested
    @DisplayName("주문 결제 동시성 테스트")
    inner class ProcessPayment {

        @Test
        @DisplayName("같은 사용자의 동시 결제 요청은 잔액이 충분한 경우, 요청만큼 차감")
        fun processPaymentBySameUser() {
            val initialBalance = BigDecimal.valueOf(10000)
            val orderAmount = BigDecimal.valueOf(1000)
            val userId = UserMock.id()
            databaseTestHelper.savedBalance(userId = userId, amount = initialBalance)
            val orderCount = 5
            val orders = (1..orderCount).map { 
                databaseTestHelper.savedOrder(
                    userId = userId,
                    status = OrderStatus.STOCK_ALLOCATED,
                )
            }
            val threadCount = orderCount
            val executor = Executors.newFixedThreadPool(threadCount)
            val successCounter = AtomicInteger(0)
            val insufficientBalanceCounter = AtomicInteger(0)
            val lockFailCounter = AtomicInteger(0)
            val otherErrorCounter = AtomicInteger(0)
            val barrier = CyclicBarrier(threadCount)
            val completionLatch = CountDownLatch(threadCount)

            orders.forEachIndexed { index, order ->
                executor.submit {
                    try {
                        barrier.await()
                        val command = PayOrderProcessorCommand(
                            orderId = order.id(),
                            userId = userId,
                            totalAmount = orderAmount
                        )
                        processor.processPayment(command)
                        successCounter.incrementAndGet()
                    } catch (e: Exception) {
                        when (e) {
                            is InsufficientBalanceException -> insufficientBalanceCounter.incrementAndGet()
                            is LockAcquisitionFailException -> lockFailCounter.incrementAndGet()
                            else -> otherErrorCounter.incrementAndGet()
                        }
                    } finally {
                        completionLatch.countDown()
                    }
                }
            }
            completionLatch.await(30, TimeUnit.SECONDS)
            executor.shutdown()

            assertThat(successCounter.get()).isEqualTo(orderCount)
            assertThat(insufficientBalanceCounter.get()).isEqualTo(0)
            assertThat(otherErrorCounter.get()).isEqualTo(0)
            assertThat(lockFailCounter.get()).isEqualTo(0)
            
            val updatedBalance = databaseTestHelper.findBalance(userId)
            require(updatedBalance != null)
            val expectedRemainingBalance = initialBalance.subtract(orderAmount.multiply(BigDecimal.valueOf(successCounter.get().toLong())))
            assertThat(updatedBalance.amount.value).isEqualByComparingTo(expectedRemainingBalance)
        }


        @Test
        @DisplayName("동시 결제 요청이 잔고를 초과하는 경우, 동시 요청은 모두 BelowMinBalanceAmountException을 발생시켜야 함")
        fun insufficientBalance() {
            val initialBalance = BigDecimal.valueOf(3000)
            val orderAmount = BigDecimal.valueOf(1000)
            val userId = UserMock.id()
            databaseTestHelper.savedBalance(userId = userId, amount = initialBalance)
            val orderCount = 5
            val orders = (1..orderCount).map {
                databaseTestHelper.savedOrder(
                    userId = userId,
                    status = OrderStatus.STOCK_ALLOCATED,
                )
            }
            val threadCount = orderCount
            val executor = Executors.newFixedThreadPool(threadCount)
            val successCounter = AtomicInteger(0)
            val insufficientBalanceCounter = AtomicInteger(0)
            val otherErrorCounter = AtomicInteger(0)
            val barrier = CyclicBarrier(threadCount)
            val completionLatch = CountDownLatch(threadCount)

            orders.forEach { order ->
                executor.submit {
                    try {
                        barrier.await()
                        val command = PayOrderProcessorCommand(
                            orderId = order.id(),
                            userId = userId,
                            totalAmount = orderAmount
                        )
                        processor.processPayment(command)
                        successCounter.incrementAndGet()
                    } catch (e: Exception) {
                        when (e) {
                            is BelowMinBalanceAmountException -> insufficientBalanceCounter.incrementAndGet()
                            else -> otherErrorCounter.incrementAndGet()
                        }
                    } finally {
                        completionLatch.countDown()
                    }
                }
            }
            completionLatch.await(30, TimeUnit.SECONDS)
            executor.shutdown()

            val expectedSuccessCount = initialBalance.toInt() / orderAmount.toInt()
            assertThat(successCounter.get()).isEqualTo(expectedSuccessCount)
            assertThat(insufficientBalanceCounter.get()).isEqualTo(orderCount - expectedSuccessCount)
            assertThat(otherErrorCounter.get()).isEqualTo(0)
            val updatedBalance = databaseTestHelper.findBalance(userId)
            require(updatedBalance != null)
            val expectedRemainingBalance = initialBalance.subtract(orderAmount.multiply(BigDecimal.valueOf(successCounter.get().toLong())))
            assertThat(updatedBalance.amount.value).isEqualByComparingTo(expectedRemainingBalance)
        }

        @Test
        @DisplayName("존재하지 않는 잔고에 대한 동시 결제 요청은 모두 NotFoundBalanceException을 발생")
        fun notFoundBalance() {
            val orderId = databaseTestHelper.savedOrder().id()
            val nonExistingUserId = UserMock.id()
            val threadCount = 5
            val executor = Executors.newFixedThreadPool(threadCount)
            val errorCount = AtomicInteger(0)
            val barrier = CyclicBarrier(threadCount)
            val completionLatch = CountDownLatch(threadCount)

            repeat(threadCount) {
                executor.submit {
                    try {
                        barrier.await()
                        val command = PayOrderProcessorCommand(
                            orderId = orderId,
                            userId = nonExistingUserId,
                            totalAmount = BigDecimal.valueOf(1000)
                        )
                        processor.processPayment(command)
                    } catch (e: Exception) {
                        errorCount.incrementAndGet()
                    } finally {
                        completionLatch.countDown()
                    }
                }
            }
            completionLatch.await(30, TimeUnit.SECONDS)
            executor.shutdown()

            assertThat(errorCount.get()).isEqualTo(threadCount)
        }
    }
}
