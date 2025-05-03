package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.application.order.command.PlaceOrderProductProcessorCommand
import kr.hhplus.be.server.domain.common.LockAcquisitionFailException
import kr.hhplus.be.server.domain.product.excpetion.NotFoundProductException
import kr.hhplus.be.server.domain.stock.exception.OutOfStockException
import kr.hhplus.be.server.testutil.DatabaseTestHelper
import kr.hhplus.be.server.testutil.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
class OrderProductProcessorConcurrencyTest {

    @Autowired
    private lateinit var processor: OrderProductProcessor

    @Autowired
    private lateinit var databaseTestHelper: DatabaseTestHelper

    @Nested
    @DisplayName("재고 할당 동시성 테스트")
    inner class AllocateStock {
        @Test
        @DisplayName("재고 수량이 충분한 경우, 요청 만큼 차감")
        fun allocate() {
            val orderId = databaseTestHelper.savedOrder().id()
            val initialQuantity = 100
            val product = databaseTestHelper.savedProduct(
                stock = initialQuantity,
            )
            val threadCount = 10
            val allocateQuantity = 5
            val executor = Executors.newFixedThreadPool(threadCount)
            val successCounter = AtomicInteger(0)
            val outOfStockCounter = AtomicInteger(0)
            val lockFailCounter = AtomicInteger(0)
            val otherErrorCounter = AtomicInteger(0)
            val barrier = CyclicBarrier(threadCount)
            val completionLatch = CountDownLatch(threadCount)

            repeat(threadCount) {
                executor.submit {
                    try {
                        barrier.await()
                        val command = PlaceOrderProductProcessorCommand(
                            orderId = orderId,
                            productId = product.id().value,
                            quantity = allocateQuantity,
                            unitPrice = product.price,
                        )
                        processor.placeOrderProduct(command)
                        successCounter.incrementAndGet()
                    } catch (e: Exception) {
                        when (e) {
                            is OutOfStockException -> outOfStockCounter.incrementAndGet()
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

            assertThat(successCounter.get()).isEqualTo(threadCount)
            assertThat(outOfStockCounter.get()).isEqualTo(0)
            assertThat(otherErrorCounter.get()).isEqualTo(0)
            assertThat(lockFailCounter.get()).isEqualTo(0)
            val updatedStock = databaseTestHelper.findStock(product.id())
            require(updatedStock != null)
            assertThat(updatedStock.quantity).isEqualTo(initialQuantity - (allocateQuantity * successCounter.get()))
        }

        @Test
        @DisplayName("재고가 부족한 경우, 동시 요청은 OutOfStockException을 발생시켜야 함")
        fun outOfStock() {
            val orderId = databaseTestHelper.savedOrder().id()
            val initialQuantity = 20
            val product = databaseTestHelper.savedProduct(
                stock = initialQuantity,
            )
            val threadCount = 10
            val allocateQuantityPerRequest = 5
            val executor = Executors.newFixedThreadPool(threadCount)
            val successCounter = AtomicInteger(0)
            val outOfStockCounter = AtomicInteger(0)
            val lockFailCounter = AtomicInteger(0)
            val otherErrorCounter = AtomicInteger(0)
            val barrier = CyclicBarrier(threadCount)
            val completionLatch = CountDownLatch(threadCount)

            repeat(threadCount) {
                executor.submit {
                    try {
                        barrier.await()
                        val command = PlaceOrderProductProcessorCommand(
                            orderId = orderId,
                            productId = product.id().value,
                            quantity = allocateQuantityPerRequest,
                            unitPrice = product.price,
                        )
                        processor.placeOrderProduct(command)
                        successCounter.incrementAndGet()
                    } catch (e: Exception) {
                        when (e) {
                            is OutOfStockException -> outOfStockCounter.incrementAndGet()
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

            val expectedSuccessCount = initialQuantity / allocateQuantityPerRequest
            assertThat(successCounter.get()).isEqualTo(expectedSuccessCount)
            assertThat(outOfStockCounter.get()).isEqualTo(threadCount - expectedSuccessCount)
            assertThat(successCounter.get() + outOfStockCounter.get()).isEqualTo(threadCount)
            assertThat(lockFailCounter.get()).isEqualTo(0)
            assertThat(otherErrorCounter.get()).isEqualTo(0)
            val updatedStock = databaseTestHelper.findStock(product.id())
            require(updatedStock != null)
            assertThat(updatedStock.quantity).isEqualTo(initialQuantity - (allocateQuantityPerRequest * successCounter.get()))
        }

        @Test
        @DisplayName("존재하지 않는 상품에 대한 동시 요청은 모두 NotFoundProductException을 발생")
        fun notFoundProduct() {
            val orderId = databaseTestHelper.savedOrder().id()
            val nonExistingProductId = ProductMock.id()
            val threadCount = 5
            val executor = Executors.newFixedThreadPool(threadCount)
            val errorCount = AtomicInteger(0)
            val barrier = CyclicBarrier(threadCount)
            val completionLatch = CountDownLatch(threadCount)

            repeat(threadCount) {
                executor.submit {
                    try {
                        barrier.await()
                        val command = PlaceOrderProductProcessorCommand(
                            orderId = orderId,
                            productId = nonExistingProductId.value,
                            quantity = 5,
                            unitPrice = 1000.toBigDecimal(),
                        )
                        processor.placeOrderProduct(command)
                    } catch (ex: NotFoundProductException) {
                        errorCount.incrementAndGet()
                    } finally {
                        completionLatch.countDown()
                    }
                }
            }
            completionLatch.await()
            executor.shutdown()

            assertThat(errorCount.get()).isEqualTo(threadCount)
        }
    }
}
