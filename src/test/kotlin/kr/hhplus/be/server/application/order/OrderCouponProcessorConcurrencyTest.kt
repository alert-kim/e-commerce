package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.application.order.command.ApplyCouponProcessorCommand
import kr.hhplus.be.server.domain.common.LockAcquisitionFailException
import kr.hhplus.be.server.domain.coupon.exception.AlreadyUsedCouponException
import kr.hhplus.be.server.domain.coupon.exception.NotFoundCouponException
import kr.hhplus.be.server.domain.order.OrderStatus
import kr.hhplus.be.server.testutil.DatabaseTestHelper
import kr.hhplus.be.server.testutil.mock.CouponMock
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
class OrderCouponProcessorConcurrencyTest {

    @Autowired
    private lateinit var processor: OrderCouponProcessor

    @Autowired
    private lateinit var databaseTestHelper: DatabaseTestHelper

    @Nested
    @DisplayName("쿠폰 적용 동시성 테스트")
    inner class ApplyCoupon {
        @Test
        @DisplayName("동일한 쿠폰을 여러 요청에서 사용하려고 할 경우, 한 요청만 성공해야 함")
        fun successOnlyOne() {
            val order = databaseTestHelper.savedOrder(status = OrderStatus.STOCK_ALLOCATED)
            val coupon = databaseTestHelper.savedCoupon(userId = order.userId)
            val threadCount = 5
            val executor = Executors.newFixedThreadPool(threadCount)
            val successCounter = AtomicInteger(0)
            val alreadyUsedCounter = AtomicInteger(0)
            val lockFailCounter = AtomicInteger(0)
            val otherErrorCounter = AtomicInteger(0)
            val barrier = CyclicBarrier(threadCount)
            val completionLatch = CountDownLatch(threadCount)

            repeat(threadCount) {
                executor.submit {
                    try {
                        barrier.await()
                        val command = ApplyCouponProcessorCommand(
                            orderId = order.id(),
                            userId = order.userId,
                            couponId = coupon.id().value
                        )
                        processor.applyCouponToOrder(command)
                        successCounter.incrementAndGet()
                    } catch (e: Exception) {
                        when (e) {
                            is AlreadyUsedCouponException -> alreadyUsedCounter.incrementAndGet()
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

            assertThat(successCounter.get()).isEqualTo(1)
            assertThat(alreadyUsedCounter.get() + lockFailCounter.get()).isEqualTo(threadCount - 1)
            assertThat(otherErrorCounter.get()).isEqualTo(0)
            val updatedCoupon = databaseTestHelper.findCoupon(coupon.id())
            require(updatedCoupon != null)
            assertThat(updatedCoupon.usedAt).isNotNull()
        }

        @Test
        @DisplayName("서로 다른 쿠폰을 여러 요청에서 사용하는 경우, 모든 요청이 성공해야 함")
        fun allSuccessWithDifferentCoupons() {
            val couponCount = 3
            val couponAndOrders = (1..couponCount).map {
                val order = databaseTestHelper.savedOrder(status = OrderStatus.STOCK_ALLOCATED)
                val coupon = databaseTestHelper.savedCoupon(userId = order.userId)
                coupon to order
            }
            val threadCount = couponCount
            val executor = Executors.newFixedThreadPool(threadCount)
            val successCounter = AtomicInteger(0)
            val errorCounter = AtomicInteger(0)
            val barrier = CyclicBarrier(threadCount)
            val completionLatch = CountDownLatch(threadCount)

            couponAndOrders.forEach { (coupon, order) ->
                executor.submit {
                    try {
                        barrier.await()
                        val command = ApplyCouponProcessorCommand(
                            orderId = order.id(),
                            userId = order.userId,
                            couponId = coupon.id().value
                        )
                        processor.applyCouponToOrder(command)
                        successCounter.incrementAndGet()
                    } catch (e: Exception) {
                        errorCounter.incrementAndGet()
                    } finally {
                        completionLatch.countDown()
                    }
                }
            }
            completionLatch.await()
            executor.shutdown()

            assertThat(successCounter.get()).isEqualTo(couponCount)
            assertThat(errorCounter.get()).isEqualTo(0)
        }
    }
}
