package kr.hhplus.be.server.mock

import kr.hhplus.be.server.application.order.command.OrderFacadeCommand
import kr.hhplus.be.server.domain.coupon.CouponId
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal

object OrderCommandMock {
    fun facade(
        userId: UserId = UserMock.id(),
        couponId: CouponId? = CouponMock.id(),
        productsToOrder: List<OrderFacadeCommand.ProductToOrder> = productsToOrders(),
        originalAmount: BigDecimal = BigDecimal.valueOf(50_000),
        discountAmount: BigDecimal = if(couponId != null) BigDecimal.valueOf(10_000) else BigDecimal.ZERO,
        totalAmount: BigDecimal = if(couponId != null) BigDecimal.valueOf(40_000) else BigDecimal.valueOf(50_000),
        ): OrderFacadeCommand =
        OrderFacadeCommand(
            userId = userId.value,
            productsToOrder = productsToOrder,
            couponId = couponId?.value,
            originalAmount = originalAmount,
            discountAmount = discountAmount,
            totalAmount = totalAmount,
        )

    fun productsToOrders(): List<OrderFacadeCommand.ProductToOrder> =
        listOf(
            OrderFacadeCommand.ProductToOrder(
                productId = 1L,
                quantity = 2,
                unitPrice = BigDecimal.valueOf(10_000),
                totalPrice = BigDecimal.valueOf(20_000)
            ),
            OrderFacadeCommand.ProductToOrder(
                productId = 2L,
                quantity = 1,
                unitPrice = BigDecimal.valueOf(30_000),
                totalPrice = BigDecimal.valueOf(30_000)
            ),
        )

    fun productToOrder(
        productId: Long = 1L,
        quantity: Int = 2,
        unitPrice: BigDecimal = BigDecimal.valueOf(10_000),
        totalPrice: BigDecimal = BigDecimal.valueOf(20_000)
    ): OrderFacadeCommand.ProductToOrder =
        OrderFacadeCommand.ProductToOrder(
            productId = productId,
            quantity = quantity,
            unitPrice = unitPrice,
            totalPrice = totalPrice
    )
}
