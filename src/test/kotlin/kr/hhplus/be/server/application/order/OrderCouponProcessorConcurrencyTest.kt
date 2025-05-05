package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.application.order.command.ApplyCouponProcessorCommand
import kr.hhplus.be.server.application.order.command.CancelCouponUseProcessorCommand
import kr.hhplus.be.server.domain.common.LockAcquisitionFailException
import kr.hhplus.be.server.domain.coupon.exception.AlreadyUsedCouponException
import kr.hhplus.be.server.domain.coupon.exception.NotFoundCouponException
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.OrderStatus
import kr.hhplus.be.server.testutil.DatabaseTestHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

@SpringBootTest
class OrderCouponProcessorConcurrencyTest {

    @Autowired
    private lateinit var orderService: OrderService

    @Autowired
    private lateinit var processor: OrderCouponProcessor

    @Autowired
    private lateinit var databaseTestHelper: DatabaseTestHelper

    @Nested
    @DisplayName("쿠폰 적용 동시성 테스트")
    inner class ApplyCoupon {
        @Test
        @DisplayName("동일한 쿠폰을 여러 요청에서 사용하려고 할 경우, 한 요청만 성공해야 함")
        fun onlyOneSucceeds() {
            val order = databaseTestHelper.savedOrder(status = OrderStatus.STOCK_ALLOCATED)
            val coupon = databaseTestHelper.savedCoupon(userId = order.userId)
            val threadCount = 5

            val executor = Executors.newFixedThreadPool(threadCount)
            val barrier = CyclicBarrier(threadCount)
            val completionLatch = CountDownLatch(threadCount)
            val successCounter = AtomicInteger(0)
            val alreadyUsedCounter = AtomicInteger(0)
            val lockFailCounter = AtomicInteger(0)
            val otherErrorCounter = AtomicInteger(0)

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
        fun allSucceedWithDifferentCoupons() {
            val couponCount = 3
            val couponAndOrders = (1..couponCount).map {
                val order = databaseTestHelper.savedOrder(status = OrderStatus.STOCK_ALLOCATED)
                val coupon = databaseTestHelper.savedCoupon(userId = order.userId)
                coupon to order
            }

            val threadCount = couponCount
            val executor = Executors.newFixedThreadPool(threadCount)
            val barrier = CyclicBarrier(threadCount)
            val completionLatch = CountDownLatch(threadCount)
            val successCounter = AtomicInteger(0)
            val errorCounter = AtomicInteger(0)

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
            couponAndOrders.forEach { (coupon, order) ->
                val updatedCoupon = databaseTestHelper.findCoupon(coupon.id())
                require(updatedCoupon != null)
                assertThat(updatedCoupon.usedAt).isNotNull()

                val updatedOrder = orderService.get(order.id().value)
                assertThat(updatedOrder.couponId).isEqualTo(coupon.id())
            }
        }
    }

    @Nested
    @DisplayName("쿠폰 취소 동시성 테스트")
    inner class CancelCoupon {
        @Test
        @DisplayName("동일한 쿠폰을 여러 요청에서 취소하려고 할 경우, 한 요청만 성공해야 함")
        fun onlyOneSucceeds() {
            val coupon = databaseTestHelper.savedCoupon()

            val threadCount = 5
            val executor = Executors.newFixedThreadPool(threadCount)
            val barrier = CyclicBarrier(threadCount)
            val completionLatch = CountDownLatch(threadCount)
            val successCounter = AtomicInteger(0)
            val lockFailCounter = AtomicInteger(0)
            val otherErrorCounter = AtomicInteger(0)

            repeat(threadCount) {
                executor.submit {
                    try {
                        barrier.await()
                        val command = CancelCouponUseProcessorCommand(coupon.id())
                        processor.cancelCoupon(command)
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

            assertThat(successCounter.get()).isEqualTo(1)
            assertThat(lockFailCounter.get()).isEqualTo(threadCount - 1)
            assertThat(otherErrorCounter.get()).isEqualTo(0)
            val updatedCoupon = databaseTestHelper.findCoupon(coupon.id())
            require(updatedCoupon != null)
            assertThat(updatedCoupon.usedAt).isNull()
        }

        @Test
        @DisplayName("서로 다른 쿠폰을 여러 요청에서 취소하는 경우, 모든 요청이 성공해야 함")
        fun allSucceedWithDifferentCoupons() {
            val couponCount = 3
            val coupons = (1..couponCount).map {
                databaseTestHelper.savedCoupon(
                    usedAt = Instant.now()
                )
            }
            
            val threadCount = couponCount
            val executor = Executors.newFixedThreadPool(threadCount)
            val barrier = CyclicBarrier(threadCount)
            val completionLatch = CountDownLatch(threadCount)
            val successCounter = AtomicInteger(0)
            val errorCounter = AtomicInteger(0)

            coupons.forEach { coupon ->
                executor.submit {
                    try {
                        barrier.await()
                        val command = CancelCouponUseProcessorCommand(coupon.id())
                        processor.cancelCoupon(command)
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
            
            coupons.forEach { coupon ->
                val updatedCoupon = databaseTestHelper.findCoupon(coupon.id())
                require(updatedCoupon != null)
                assertThat(updatedCoupon.usedAt).isNull()
            }
        }
    }

    @Nested
    @DisplayName("쿠폰 적용과 취소 동시 요청 테스트")
    inner class ApplyAndCancelCoupon {
        @Test
        @DisplayName("동일한 쿠폰에 대해 적용과 취소를 동시에 요청하면 한 요청만 성공해야 함")
        fun onlyOneSucceeds() {
            val order = databaseTestHelper.savedOrder(status = OrderStatus.STOCK_ALLOCATED)
            val coupon = databaseTestHelper.savedCoupon(userId = order.userId)

            val executor = Executors.newFixedThreadPool(2)
            val barrier = CyclicBarrier(2)
            val completionLatch = CountDownLatch(2)
            val applySuccess = AtomicReference<Boolean>(null)
            val cancelSuccess = AtomicReference<Boolean>(null)
            val lockFailCounter = AtomicInteger(0)
            val otherErrorCounter = AtomicInteger(0)

            executor.submit {
                try {
                    barrier.await()
                    val command = ApplyCouponProcessorCommand(
                        orderId = order.id(),
                        userId = order.userId,
                        couponId = coupon.id().value
                    )
                    processor.applyCouponToOrder(command)
                    applySuccess.set(true)
                } catch (e: Exception) {
                    applySuccess.set(false)
                    when (e) {
                        is LockAcquisitionFailException -> lockFailCounter.incrementAndGet()
                        else -> otherErrorCounter.incrementAndGet()
                    }
                } finally {
                    completionLatch.countDown()
                }
            }
            executor.submit {
                try {
                    barrier.await()
                    val command = CancelCouponUseProcessorCommand(coupon.id())
                    processor.cancelCoupon(command)
                    cancelSuccess.set(true)
                } catch (e: Exception) {
                    cancelSuccess.set(false)
                    when (e) {
                        is LockAcquisitionFailException -> lockFailCounter.incrementAndGet()
                        else -> otherErrorCounter.incrementAndGet()
                    }
                } finally {
                    completionLatch.countDown()
                }
            }
            completionLatch.await()
            executor.shutdown()

            assertThat(lockFailCounter.get()).isEqualTo(1)
            assertThat(otherErrorCounter.get()).isEqualTo(0)
            val updatedCoupon = databaseTestHelper.findCoupon(coupon.id())
            require(updatedCoupon != null)
            when {
                applySuccess.get() == true  -> {
                    assertThat(cancelSuccess.get()).isFalse()
                    assertThat(updatedCoupon.usedAt).isNotNull()
                }
                cancelSuccess.get() == true -> {
                    assertThat(applySuccess.get()).isFalse()
                    assertThat(updatedCoupon.usedAt).isNull()
                }
            }
        }

        @Test
        @DisplayName("서로 다른 쿠폰에 대해 적용과 취소를 동시에 요청하면 모든 요청이 성공해야 함")
        fun allSucceedWithDifferentCoupons() {
            val order = databaseTestHelper.savedOrder(status = OrderStatus.STOCK_ALLOCATED)
            val couponToUse = databaseTestHelper.savedCoupon(userId = order.userId)
            val couponToCancelUsage = databaseTestHelper.savedCoupon(
                userId = order.userId,
                usedAt = Instant.now()
            )

            val executor = Executors.newFixedThreadPool(2)
            val barrier = CyclicBarrier(2)
            val completionLatch = CountDownLatch(2)
            val successCounter = AtomicInteger(0)
            val errorCounter = AtomicInteger(0)

            executor.submit {
                try {
                    barrier.await()
                    val command = ApplyCouponProcessorCommand(
                        orderId = order.id(),
                        userId = order.userId,
                        couponId = couponToUse.id().value
                    )
                    processor.applyCouponToOrder(command)
                    successCounter.incrementAndGet()
                } catch (e: Exception) {
                    errorCounter.incrementAndGet()
                } finally {
                    completionLatch.countDown()
                }
            }

            executor.submit {
                try {
                    barrier.await()
                    val command = CancelCouponUseProcessorCommand(couponToCancelUsage.id())
                    processor.cancelCoupon(command)
                    successCounter.incrementAndGet()
                } catch (e: Exception) {
                    errorCounter.incrementAndGet()
                } finally {
                    completionLatch.countDown()
                }
            }
            completionLatch.await()
            executor.shutdown()

            assertThat(successCounter.get()).isEqualTo(2)
            assertThat(errorCounter.get()).isEqualTo(0)
            val usedCoupon = databaseTestHelper.findCoupon(couponToUse.id())
            require(usedCoupon != null)
            assertThat(usedCoupon.usedAt).isNotNull()
            val usagedCanceledCoupon = databaseTestHelper.findCoupon(couponToCancelUsage.id())
            require(usagedCanceledCoupon != null)
            assertThat(usagedCanceledCoupon.usedAt).isNull()
        }
    }
}
