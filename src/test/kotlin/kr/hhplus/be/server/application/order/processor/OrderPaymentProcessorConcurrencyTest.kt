package kr.hhplus.be.server.application.order.processor

import kr.hhplus.be.server.application.order.command.CancelOrderPaymentProcessorCommand
import kr.hhplus.be.server.application.order.command.PayOrderProcessorCommand
import kr.hhplus.be.server.domain.balance.exception.BelowMinBalanceAmountException
import kr.hhplus.be.server.domain.balance.exception.InsufficientBalanceException
import kr.hhplus.be.server.domain.common.LockAcquisitionFailException
import kr.hhplus.be.server.domain.order.OrderStatus
import kr.hhplus.be.server.domain.payment.exception.NotOwnedPaymentException
import kr.hhplus.be.server.testutil.DatabaseTestHelper
import kr.hhplus.be.server.testutil.mock.OrderMock
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions
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
import java.util.concurrent.atomic.AtomicReference

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

            Assertions.assertThat(successCounter.get()).isEqualTo(orderCount)
            Assertions.assertThat(insufficientBalanceCounter.get()).isEqualTo(0)
            Assertions.assertThat(otherErrorCounter.get()).isEqualTo(0)
            Assertions.assertThat(lockFailCounter.get()).isEqualTo(0)
            val updatedBalance = databaseTestHelper.findBalance(userId)
            require(updatedBalance != null)
            val expectedRemainingBalance =
                initialBalance.subtract(orderAmount.multiply(BigDecimal.valueOf(successCounter.get().toLong())))
            Assertions.assertThat(updatedBalance.amount.value).isEqualByComparingTo(expectedRemainingBalance)
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
            Assertions.assertThat(successCounter.get()).isEqualTo(expectedSuccessCount)
            Assertions.assertThat(insufficientBalanceCounter.get()).isEqualTo(orderCount - expectedSuccessCount)
            Assertions.assertThat(otherErrorCounter.get()).isEqualTo(0)
            val updatedBalance = databaseTestHelper.findBalance(userId)
            require(updatedBalance != null)
            val expectedRemainingBalance =
                initialBalance.subtract(orderAmount.multiply(BigDecimal.valueOf(successCounter.get().toLong())))
            Assertions.assertThat(updatedBalance.amount.value).isEqualByComparingTo(expectedRemainingBalance)
        }

        @Test
        @DisplayName("존재하지 않는 잔고에 대한 동시 결제 요청은 모두 NotFoundBalanceException을 발생")
        fun notFoundBalance() {
            val orderId = OrderMock.id()
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

            Assertions.assertThat(errorCount.get()).isEqualTo(threadCount)
        }
    }

    @Nested
    @DisplayName("결제 취소 동시성 테스트")
    inner class CancelPayment {

        @Test
        @DisplayName("같은 사용자의 동시 결제 취소 요청은 모두 성공해야 한다")
        fun cancel() {
            val initialBalance = 5000.toBigDecimal()
            val cancelAmount = 1000.toBigDecimal()
            val userId = UserMock.id()
            databaseTestHelper.savedBalance(userId = userId, amount = initialBalance)
            val requestCount = 5
            val orderIds = (1..requestCount).map {
                val orderId = OrderMock.id()
                databaseTestHelper.savedPayment(
                    userId = userId,
                    orderId = orderId,
                    amount = cancelAmount
                )
                orderId
            }

            val threadCount = orderIds.size
            val executor = Executors.newFixedThreadPool(threadCount)
            val successCounter = AtomicInteger(0)
            val lockFailCounter = AtomicInteger(0)
            val otherErrorCounter = AtomicInteger(0)
            val barrier = CyclicBarrier(threadCount)
            val completionLatch = CountDownLatch(threadCount)

            orderIds.forEach { orderId ->
                executor.submit {
                    try {
                        barrier.await()
                        val command = CancelOrderPaymentProcessorCommand(
                            orderId = orderId,
                            userId = userId
                        )
                        processor.cancelPayment(command)
                        successCounter.incrementAndGet()
                    } catch (e: Exception) {
                        when (e) {
                            is LockAcquisitionFailException -> lockFailCounter.incrementAndGet()
                            else -> otherErrorCounter.incrementAndGet()
                        }
                    } finally {
                        completionLatch.countDown()
                    }
                }
            }
            completionLatch.await()
            executor.shutdown()

            Assertions.assertThat(successCounter.get()).isEqualTo(orderIds.size)
            Assertions.assertThat(otherErrorCounter.get()).isEqualTo(0)
            Assertions.assertThat(lockFailCounter.get()).isEqualTo(0)
            val updatedBalance = databaseTestHelper.findBalance(userId)
            require(updatedBalance != null)
            Assertions.assertThat(updatedBalance.amount.value).isEqualByComparingTo(
                initialBalance.add(cancelAmount.multiply(orderIds.size.toBigDecimal()))
            )
        }

        @Test
        @DisplayName("다른 사용자의 결제 취소 시도가 동시에 이루어지면 모두 NotOwnedPaymentException을 발생시켜야 한다")
        fun cancelNotOwnedPayment() {
            val initialBalance = 5000.toBigDecimal()
            val userId = UserMock.id(1)
            databaseTestHelper.savedBalance(userId = userId, amount = initialBalance)
            val orderId = OrderMock.id()
            databaseTestHelper.savedPayment(
                userId = userId,
                orderId = orderId,
            )
            val differentUserId = UserMock.id(2)

            val threadCount = 5
            val executor = Executors.newFixedThreadPool(threadCount)
            val notOwnedExCounter = AtomicInteger(0)
            val otherExCounter = AtomicInteger(0)
            val barrier = CyclicBarrier(threadCount)
            val completionLatch = CountDownLatch(threadCount)

            repeat(threadCount) {
                executor.submit {
                    try {
                        barrier.await()
                        val command = CancelOrderPaymentProcessorCommand(
                            orderId = orderId,
                            userId = differentUserId
                        )
                        processor.cancelPayment(command)
                    } catch (e: Exception) {
                        when (e) {
                            is NotOwnedPaymentException -> notOwnedExCounter.incrementAndGet()
                            else -> otherExCounter.incrementAndGet()
                        }
                    } finally {
                        completionLatch.countDown()
                    }
                }
            }
            completionLatch.await(30, TimeUnit.SECONDS)
            executor.shutdown()

            Assertions.assertThat(notOwnedExCounter.get()).isEqualTo(threadCount)
            Assertions.assertThat(otherExCounter.get()).isEqualTo(0)
            val updatedBalance = databaseTestHelper.findBalance(userId)
            require(updatedBalance != null)
            Assertions.assertThat(updatedBalance.amount.value).isEqualByComparingTo(initialBalance)
        }

        @Test
        @DisplayName("결제와 취소가 동시에 요청되면 모두 성공해야 한다")
        fun payAndCancel() {
            val initialBalance = 10000.toBigDecimal()
            val payAmount = 1000.toBigDecimal()
            val cancelAmount = 2000.toBigDecimal()
            val userId = UserMock.id()
            databaseTestHelper.savedBalance(userId = userId, amount = initialBalance)
            val orderToPay = databaseTestHelper.savedOrder(userId = userId, status = OrderStatus.STOCK_ALLOCATED)
            val orderIdToCancel = OrderMock.id()
            val paymentToCancel = databaseTestHelper.savedPayment(
                userId = userId,
                orderId = orderIdToCancel,
                amount = cancelAmount
            )
            val executor = Executors.newFixedThreadPool(2)
            val paySuccess = AtomicReference<Boolean>(null)
            val cancelSuccess = AtomicReference<Boolean>(null)
            val barrier = CyclicBarrier(2)
            val completionLatch = CountDownLatch(2)

            executor.submit {
                try {
                    barrier.await()
                    val command = PayOrderProcessorCommand(
                        orderId = orderToPay.id(),
                        userId = userId,
                        totalAmount = payAmount,
                    )
                    processor.processPayment(command)
                    paySuccess.set(true)
                } catch (e: Exception) {
                    paySuccess.set(false)
                } finally {
                    completionLatch.countDown()
                }
            }
            executor.submit {
                try {
                    barrier.await()
                    val command = CancelOrderPaymentProcessorCommand(
                        orderId = orderIdToCancel,
                        userId = userId
                    )
                    processor.cancelPayment(command)
                    cancelSuccess.set(true)
                } catch (e: Exception) {
                    cancelSuccess.set(false)
                } finally {
                    completionLatch.countDown()
                }
            }
            completionLatch.await(30, TimeUnit.SECONDS)
            executor.shutdown()

            Assertions.assertThat(paySuccess.get()).isTrue
            Assertions.assertThat(cancelSuccess.get()).isTrue
            val updatedBalance = databaseTestHelper.findBalance(userId)
            require(updatedBalance != null)
            Assertions.assertThat(updatedBalance.amount.value).isEqualByComparingTo(
                initialBalance.subtract(payAmount).add(cancelAmount)
            )
        }
    }

}
