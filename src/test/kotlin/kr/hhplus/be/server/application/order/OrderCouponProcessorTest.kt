package kr.hhplus.be.server.application.order

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kr.hhplus.be.server.application.order.command.ApplyCouponProcessorCommand
import kr.hhplus.be.server.application.order.command.CancelCouponUseProcessorCommand
import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.domain.coupon.command.CancelCouponUseCommand
import kr.hhplus.be.server.domain.coupon.command.UseCouponCommand
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.command.ApplyCouponCommand
import kr.hhplus.be.server.testutil.mock.CouponMock
import kr.hhplus.be.server.testutil.mock.OrderMock
import kr.hhplus.be.server.testutil.mock.UserMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OrderCouponProcessorTest {

    private lateinit var processor: OrderCouponProcessor

    private val couponService = mockk<CouponService>(relaxed = true)
    private val orderService = mockk<OrderService>(relaxed = true)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        processor = OrderCouponProcessor(
            couponService = couponService,
            orderService = orderService
        )
    }

    @Nested
    @DisplayName("쿠폰 적용 처리")
    inner class ApplyCoupon {
        @Test
        @DisplayName("쿠폰을 주문에 적용")
        fun applyCouponToOrder() {
            val orderId = OrderMock.id()
            val userId = UserMock.id()
            val couponId = CouponMock.id()
            val usedCoupon = CouponMock.usedCoupon(id = couponId)
            every { couponService.use(UseCouponCommand(couponId.value, userId)) } returns usedCoupon

            val command = ApplyCouponProcessorCommand(
                orderId = orderId,
                userId = userId,
                couponId = couponId.value
            )
            processor.applyCouponToOrder(command)

            verifyOrder {
                couponService.use(UseCouponCommand(couponId.value, userId))
                orderService.applyCoupon(ApplyCouponCommand(orderId, usedCoupon))
            }
        }
    }

    @Nested
    @DisplayName("쿠폰 사용 취소")
    inner class CancelCouponUse {
        @Test
        @DisplayName("쿠폰 사용 취소 처리")
        fun cancelCoupon() {
            val couponId = CouponMock.id()

            val command = CancelCouponUseProcessorCommand(couponId)
            processor.cancelCoupon(command)

            verify {
                couponService.cancelUse(CancelCouponUseCommand(couponId.value))
            }
        }
    }
}
