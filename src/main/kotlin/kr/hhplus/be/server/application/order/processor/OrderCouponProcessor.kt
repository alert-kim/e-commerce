package kr.hhplus.be.server.application.order.processor

import kr.hhplus.be.server.application.order.command.ApplyCouponProcessorCommand
import kr.hhplus.be.server.application.order.command.CancelCouponUseProcessorCommand
import kr.hhplus.be.server.common.lock.LockStrategy
import kr.hhplus.be.server.common.lock.annotation.DistributedLock
import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.domain.coupon.command.CancelCouponUseCommand
import kr.hhplus.be.server.domain.coupon.command.UseCouponCommand
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.command.ApplyCouponCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@Service
class OrderCouponProcessor(
    private val couponService: CouponService,
    private val orderService: OrderService,
) {

    @DistributedLock(
        keyPrefix = "coupon",
        identifier = "#command.couponId",
        strategy = LockStrategy.SIMPLE,
        leaseTime = 2_000,
        timeUnit = TimeUnit.MILLISECONDS,
    )
    @Transactional
    fun applyCouponToOrder(command: ApplyCouponProcessorCommand) {
        val usedCoupon = couponService.use(
            UseCouponCommand(command.couponId, command.userId)
        )

        orderService.applyCoupon(
            ApplyCouponCommand(command.orderId, usedCoupon)
        )
    }

    @DistributedLock(
        keyPrefix = "coupon",
        identifier = "#command.couponId.value",
        strategy = LockStrategy.SIMPLE,
        leaseTime = 2_000,
        timeUnit = TimeUnit.MILLISECONDS,
    )
    fun cancelCoupon(command: CancelCouponUseProcessorCommand) {
        couponService.cancelUse(
            CancelCouponUseCommand(command.couponId.value)
        )
    }
}
