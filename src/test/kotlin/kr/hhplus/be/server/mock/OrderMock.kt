package kr.hhplus.be.server.mock

import kr.hhplus.be.server.domain.coupon.CouponId
import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.order.OrderProductQueryModel
import kr.hhplus.be.server.domain.order.OrderQueryModel
import kr.hhplus.be.server.domain.order.OrderStatus
import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

object OrderMock {
    fun id(): OrderId = OrderId(IdMock.value())

    fun orderProductQueryModel(
        orderId: OrderId = id(),
        productId: Long = IdMock.value(),
        quantity: Int = 1,
        unitPrice: BigDecimal = BigDecimal.valueOf(1_000),
        totalPrice: BigDecimal = BigDecimal.valueOf(2_000),
        createdAt: Instant = Instant.now(),
    ): OrderProductQueryModel = OrderProductQueryModel(
        orderId = orderId,
        productId = ProductId(productId),
        quantity = quantity,
        unitPrice = unitPrice,
        totalPrice = totalPrice,
        createdAt = createdAt,
    )

    fun orderQueryModel(
        id: OrderId = id(),
        userId: UserId = UserMock.id(),
        status: OrderStatus = OrderStatus.READY,
        couponId: CouponId? = CouponMock.id(),
        originalAmount: BigDecimal = BigDecimal.valueOf(2_000),
        discountAmount: BigDecimal = BigDecimal.valueOf(1_000),
        totalAmount: BigDecimal = BigDecimal.valueOf(1_000),
        items: List<OrderProductQueryModel> = listOf(orderProductQueryModel()),
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) = OrderQueryModel(
        id = id,
        userId = userId,
        status = status,
        couponId = couponId,
        originalAmount = originalAmount,
        discountAmount = discountAmount,
        totalAmount = totalAmount,
        products = items,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
