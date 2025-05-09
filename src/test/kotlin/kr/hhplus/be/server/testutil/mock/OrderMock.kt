package kr.hhplus.be.server.testutil.mock

import kr.hhplus.be.server.domain.coupon.CouponId
import kr.hhplus.be.server.domain.order.*
import kr.hhplus.be.server.domain.order.event.OrderCompletedEvent
import kr.hhplus.be.server.domain.order.event.OrderFailedEvent
import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

object OrderMock {
    fun id(): OrderId = OrderId(IdMock.value())

    fun productId(): OrderProductId = OrderProductId(IdMock.value())

    fun product(
        id: OrderProductId? = productId(),
        order: Order? = null,
        productId: ProductId = ProductMock.id(),
        quantity: Int = 1,
        unitPrice: BigDecimal = BigDecimal.valueOf(1_000),
        totalPrice: BigDecimal = BigDecimal.valueOf(2_000),
        createdAt: Instant = Instant.now(),
    ): OrderProduct = OrderProduct(
        id = id?.value,
        order = order,
        productId = productId,
        quantity = quantity,
        unitPrice = unitPrice,
        totalPrice = totalPrice,
        createdAt = createdAt,
    )

    fun order(
        id: OrderId? = id(),
        userId: UserId = UserMock.id(),
        status: OrderStatus = OrderStatus.STOCK_ALLOCATED,
        couponId: CouponId? = CouponMock.id(),
        originalAmount: BigDecimal = BigDecimal.valueOf(2_000),
        discountAmount: BigDecimal = BigDecimal.valueOf(1_000),
        totalAmount: BigDecimal = BigDecimal.valueOf(1_000),
        products: List<OrderProduct> = listOf(product()),
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) = Order(
        id = id?.value,
        userId = userId,
        status = status,
        couponId = couponId,
        originalAmount = originalAmount,
        discountAmount = discountAmount,
        totalAmount = totalAmount,
        orderProducts = products,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    fun productView(
        productId: Long = IdMock.value(),
        quantity: Int = 1,
        unitPrice: BigDecimal = BigDecimal.valueOf(1_000),
        totalPrice: BigDecimal = BigDecimal.valueOf(2_000),
        createdAt: Instant = Instant.now(),
    ): OrderProductView = OrderProductView(
        productId = ProductId(productId),
        quantity = quantity,
        unitPrice = unitPrice,
        totalPrice = totalPrice,
        createdAt = createdAt,
    )

    fun view(
        id: OrderId = id(),
        userId: UserId = UserMock.id(),
        status: OrderStatus = OrderStatus.STOCK_ALLOCATED,
        couponId: CouponId? = CouponMock.id(),
        originalAmount: BigDecimal = BigDecimal.valueOf(2_000),
        discountAmount: BigDecimal = BigDecimal.valueOf(1_000),
        totalAmount: BigDecimal = BigDecimal.valueOf(1_000),
        products: List<OrderProductView> = listOf(productView()),
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) = OrderView(
        id = id,
        userId = userId,
        status = status,
        couponId = couponId,
        originalAmount = originalAmount,
        discountAmount = discountAmount,
        totalAmount = totalAmount,
        products = products,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    fun completedEvent(
        orderId: OrderId = id(),
        snapshot: OrderSnapshot = OrderSnapshot.from(order(status = OrderStatus.COMPLETED)),
        createdAt: Instant = Instant.now(),
    ): OrderCompletedEvent = OrderCompletedEvent(
        orderId = orderId,
        snapshot = snapshot,
        createdAt = createdAt,
    )

    fun failedEvent(
        orderId: OrderId = id(),
        snapshot: OrderSnapshot = OrderSnapshot.from(order(status = OrderStatus.FAILED)),
        createdAt: Instant = Instant.now(),
    ): OrderFailedEvent = OrderFailedEvent(
        orderId = orderId,
        snapshot = snapshot,
        createdAt = createdAt,
    )

    fun orderProductSnapshot(
        productId: ProductId = ProductMock.id(),
        quantity: Int = 2,
        unitPrice: BigDecimal = BigDecimal.valueOf(1_000),
        totalPrice: BigDecimal = BigDecimal.valueOf(2_000),
        createdAt: Instant = Instant.now(),
    ): OrderSnapshot.OrderProductSnapshot = OrderSnapshot.OrderProductSnapshot(
        productId = productId,
        quantity = quantity,
        unitPrice = unitPrice,
        totalPrice = totalPrice,
        createdAt = createdAt,
    )
}
