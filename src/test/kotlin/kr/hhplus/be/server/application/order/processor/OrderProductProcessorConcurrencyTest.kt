package kr.hhplus.be.server.application.order.processor

import kr.hhplus.be.server.application.order.command.PlaceOrderProductProcessorCommand
import kr.hhplus.be.server.application.order.command.RestoreStockOrderProductProcessorCommand
import kr.hhplus.be.server.domain.common.LockAcquisitionFailException
import kr.hhplus.be.server.domain.product.excpetion.NotFoundProductException
import kr.hhplus.be.server.domain.stock.exception.NotFoundStockException
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
import java.util.concurrent.atomic.AtomicReference

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
            val allocateQuantity = 5

            val threadCount = 10
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

    @Nested
    @DisplayName("재고 복구 동시성 테스트")
    inner class RestoreStock {
        @Test
        @DisplayName("동시에 재고를 복구하면 모든 요청이 성공해야 함")
        fun restore() {
            val initialQuantity = 10
            val product = databaseTestHelper.savedProduct(stock = initialQuantity)
            val restoreQuantity = 5

            val threadCount = 10
            val executor = Executors.newFixedThreadPool(threadCount)
            val successCounter = AtomicInteger(0)
            val lockFailCounter = AtomicInteger(0)
            val otherErrorCounter = AtomicInteger(0)
            val barrier = CyclicBarrier(threadCount)
            val completionLatch = CountDownLatch(threadCount)

            repeat(threadCount) {
                executor.submit {
                    try {
                        barrier.await()
                        val command = RestoreStockOrderProductProcessorCommand(
                            productId = product.id(),
                            quantity = restoreQuantity
                        )
                        processor.restoreOrderProductStock(command)
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

            assertThat(successCounter.get()).isEqualTo(threadCount)
            assertThat(otherErrorCounter.get()).isEqualTo(0)
            assertThat(lockFailCounter.get()).isEqualTo(0)
            val updatedStock = databaseTestHelper.findStock(product.id())
            require(updatedStock != null)
            assertThat(updatedStock.quantity).isEqualTo(initialQuantity + (restoreQuantity * threadCount))
        }

        @Test
        @DisplayName("존재하지 않는 상품에 대한 동시 요청은 모두 NotFoundStockException을 발생")
        fun notFoundProduct() {
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
                        val command = RestoreStockOrderProductProcessorCommand(
                            productId = nonExistingProductId,
                            quantity = 20,
                        )
                        processor.restoreOrderProductStock(command)
                    } catch (ex: NotFoundStockException) {
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

    @Nested
    @DisplayName("재공 할당과 복구에 동시 요청 테스트")
    inner class AllocateAndRestore {

        @Test
        @DisplayName("동일한 상품에 대해 재고 할당과 재고 원복 요청이 동시에 들어오면, 모든 요청이 성공해야 함")
        fun allSucceed() {
            val initialQuantity = 10
            val product = databaseTestHelper.savedProduct(stock = initialQuantity)
            val orderId = databaseTestHelper.savedOrder().id()
            val allocateQuantity = 3
            val restoreQuantity = 4

            val executor = Executors.newFixedThreadPool(2)
            val barrier = CyclicBarrier(2)
            val completionLatch = CountDownLatch(2)
            val allocateSuccess = AtomicReference<Boolean>(null)
            val restoreSuccess = AtomicReference<Boolean>(null)

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
                    allocateSuccess.set(true)
                } catch (e: Exception) {
                    allocateSuccess.set(false)
                } finally {
                    completionLatch.countDown()
                }
            }
            executor.submit {
                try {
                    barrier.await()
                    val command = RestoreStockOrderProductProcessorCommand(
                        productId = product.id(),
                        quantity = restoreQuantity
                    )
                    processor.restoreOrderProductStock(command)
                    restoreSuccess.set(true)
                } catch (e: Exception) {
                    restoreSuccess.set(false)
                } finally {
                    completionLatch.countDown()
                }
            }
            completionLatch.await()
            executor.shutdown()

            assertThat(allocateSuccess.get()).isTrue
            assertThat(restoreSuccess.get()).isTrue
            val updatedStock = databaseTestHelper.findStock(product.id())
            require(updatedStock != null)
            assertThat(updatedStock.quantity).isEqualTo(initialQuantity - allocateQuantity + restoreQuantity)
        }
    }
}
