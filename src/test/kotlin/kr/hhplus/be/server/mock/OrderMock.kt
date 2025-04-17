package kr.hhplus.be.server.mock

import kr.hhplus.be.server.domain.coupon.CouponId
import kr.hhplus.be.server.domain.order.*
import kr.hhplus.be.server.domain.order.dto.OrderSnapshot
import kr.hhplus.be.server.domain.order.event.OrderEvent
import kr.hhplus.be.server.domain.order.event.OrderEventType
import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

object OrderMock {
    fun id(): OrderId = OrderId(IdMock.value())

    fun orderProduct(
        orderId: OrderId = id(),
        productId: Long = IdMock.value(),
        quantity: Int = 1,
        unitPrice: BigDecimal = BigDecimal.valueOf(1_000),
        totalPrice: BigDecimal = BigDecimal.valueOf(2_000),
        createdAt: Instant = Instant.now(),
    ): OrderProduct = OrderProduct(
        orderId = orderId,
        productId = ProductId(productId),
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
        products: List<OrderProduct> = listOf(orderProduct()),
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) = Order(
        id = id,
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
        status: OrderStatus = OrderStatus.STOCK_ALLOCATED,
        couponId: CouponId? = CouponMock.id(),
        originalAmount: BigDecimal = BigDecimal.valueOf(2_000),
        discountAmount: BigDecimal = BigDecimal.valueOf(1_000),
        totalAmount: BigDecimal = BigDecimal.valueOf(1_000),
        products: List<OrderProductQueryModel> = listOf(orderProductQueryModel()),
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
        products = products,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    fun orderSheetProduct(
        productId: Long = IdMock.value(),
        quantity: Int = 2,
        unitPrice: BigDecimal = BigDecimal.valueOf(1_000),
        totalPrice: BigDecimal = BigDecimal.valueOf(2_000),
    ): OrderSheet.OrderProduct = OrderSheet.OrderProduct(
        productId = productId,
        quantity = quantity,
        unitPrice = unitPrice,
        totalPrice = totalPrice,
    )

    fun orderSheet(
        orderId: OrderId = id(),
        userId: UserId = UserMock.id(),
        orderProducts: List<OrderSheet.OrderProduct> = listOf(orderSheetProduct()),
        couponId: Long? = CouponMock.id().value,
        originalAmount: BigDecimal = BigDecimal.valueOf(2_000),
        discountAmount: BigDecimal = BigDecimal.valueOf(1_000),
        totalAmount: BigDecimal = BigDecimal.valueOf(1_000),
    ): OrderSheet = OrderSheet(
        orderId = orderId,
        userId = userId,
        orderProducts = orderProducts,
        couponId = couponId,
        originalAmount = originalAmount,
        discountAmount = discountAmount,
        totalAmount = totalAmount,
    )

    fun event(
        orderId: OrderId = id(),
        type: OrderEventType = OrderEventType.COMPLETED,
        snapshot: OrderSnapshot = OrderSnapshot.from(order()),
        createdAt: Instant = Instant.now(),
    ): OrderEvent = OrderEvent(
        orderId = orderId,
        type = type,
        snapshot = snapshot,
        createdAt = createdAt,
    )
}
