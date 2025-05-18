package kr.hhplus.be.server.domain.coupon


import kr.hhplus.be.server.domain.common.LockAcquisitionFailException
import kr.hhplus.be.server.domain.coupon.command.IssueCouponCommand
import kr.hhplus.be.server.domain.coupon.exception.NotFoundCouponSourceException
import kr.hhplus.be.server.domain.coupon.exception.OutOfStockCouponSourceException
import kr.hhplus.be.server.testutil.DatabaseTestHelper
import kr.hhplus.be.server.testutil.mock.CouponMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
class CouponSourceServiceConcurrencyTest @Autowired constructor(
    private val couponSourceService: CouponSourceService,
    private val databaseTestHelper: DatabaseTestHelper
) {

    @Nested
    inner class Issue {
        @Test
        fun sufficientStock() {
            val initialQuantity = 30
            val couponSource = databaseTestHelper.savedCouponSource(
                quantity = initialQuantity,
            )
            val threadCount = 20
            val executorService = Executors.newFixedThreadPool(threadCount)
            val successCounter = AtomicInteger(0)
            val outOfStockCounter = AtomicInteger(0)
            val lockFailCounter = AtomicInteger(0)
            val otherErrorCounter = AtomicInteger(0)
            val barrier = CyclicBarrier(threadCount)
            val completionLatch = CountDownLatch(threadCount)

            repeat(threadCount) {
                executorService.submit {
                    try {
                        barrier.await()
                        val command = IssueCouponCommand(couponSource.id().value)
                        couponSourceService.issue(command)
                        successCounter.incrementAndGet()
                    } catch (e: Exception) {
                        when (e) {
                            is OutOfStockCouponSourceException -> outOfStockCounter.incrementAndGet()
                            is LockAcquisitionFailException -> lockFailCounter.incrementAndGet()
                            else -> otherErrorCounter.incrementAndGet()
                        }
                    } finally {
                        completionLatch.countDown()
                    }
                }
            }
            completionLatch.await()
            executorService.shutdown()

            assertThat(successCounter.get()).isEqualTo(threadCount)
            assertThat(outOfStockCounter.get()).isEqualTo(0)
            assertThat(otherErrorCounter.get()).isEqualTo(0)
            assertThat(lockFailCounter.get()).isEqualTo(0)
            val updatedCouponSource = databaseTestHelper.findCouponSource(couponSource.id())
            require(updatedCouponSource != null)
            assertThat(updatedCouponSource.quantity).isEqualTo(initialQuantity - threadCount)
            assertThat(updatedCouponSource.status).isEqualTo(CouponSourceStatus.ACTIVE)
        }

        @Test
        fun insufficientStock() {
            val initialQuantity = 10
            val couponSource = databaseTestHelper.savedCouponSource(
                quantity = initialQuantity,
            )
            val threadCount = 20
            val executorService = Executors.newFixedThreadPool(threadCount)
            val successCounter = AtomicInteger(0)
            val outOfStockCounter = AtomicInteger(0)
            val lockFailCounter = AtomicInteger(0)
            val otherErrorCounter = AtomicInteger(0)
            val barrier = CyclicBarrier(threadCount)
            val completionLatch = CountDownLatch(threadCount)

            repeat(threadCount) {
                executorService.submit {
                    try {
                        barrier.await()
                        val command = IssueCouponCommand(couponSource.id().value)
                        couponSourceService.issue(command)
                        successCounter.incrementAndGet()
                    } catch (e: Exception) {
                        when (e) {
                            is OutOfStockCouponSourceException -> outOfStockCounter.incrementAndGet()
                            is LockAcquisitionFailException -> lockFailCounter.incrementAndGet()
                            else -> otherErrorCounter.incrementAndGet()
                        }
                    } finally {
                        completionLatch.countDown()
                    }
                }
            }
            completionLatch.await()
            executorService.shutdown()

            assertThat(successCounter.get()).isEqualTo(initialQuantity)
            assertThat(outOfStockCounter.get()).isEqualTo(threadCount - initialQuantity)
            assertThat(otherErrorCounter.get()).isEqualTo(0)
            assertThat(lockFailCounter.get()).isEqualTo(0)
            val updatedCouponSource = databaseTestHelper.findCouponSource(couponSource.id())
            require(updatedCouponSource != null)
            assertThat(updatedCouponSource.quantity).isEqualTo(0)
            assertThat(updatedCouponSource.status).isEqualTo(CouponSourceStatus.OUT_OF_STOCK)
        }

        @Test
        fun nonExistingSource() {
            val nonExistingCouponSourceId = CouponMock.sourceId()
            val threadCount = 5
            val executor = Executors.newFixedThreadPool(threadCount)
            val errorCount = AtomicInteger(0)
            val barrier = CyclicBarrier(threadCount)
            val completionLatch = CountDownLatch(threadCount)

            repeat(threadCount) {
                executor.submit {
                    try {
                        barrier.await()
                        val command = IssueCouponCommand(nonExistingCouponSourceId.value)
                        couponSourceService.issue(command)
                    } catch (ex: NotFoundCouponSourceException) {
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
