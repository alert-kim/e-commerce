package kr.hhplus.be.server.application.order

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import io.mockk.verifyOrder
import kr.hhplus.be.server.application.order.command.ApplyCouponProcessorCommand
import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.domain.coupon.command.UseCouponCommand
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.command.ApplyCouponCommand
import kr.hhplus.be.server.testutil.mock.CouponMock
import kr.hhplus.be.server.testutil.mock.OrderMock
import kr.hhplus.be.server.testutil.mock.UserMock
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class OrderCouponProcessorTest {

    @InjectMockKs
    private lateinit var processor: OrderCouponProcessor

    @MockK(relaxed = true)
    private lateinit var couponService: CouponService

    @MockK(relaxed = true)
    private lateinit var orderService: OrderService

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
}
