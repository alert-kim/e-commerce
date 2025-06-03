package kr.hhplus.be.server.domain.order.event

import kr.hhplus.be.server.domain.coupon.CouponId
import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.order.OrderProduct
import kr.hhplus.be.server.domain.order.OrderView
import kr.hhplus.be.server.domain.order.exception.InvalidOrderCouponException
import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

sealed interface OrderEvent {
    val orderId: OrderId
    val createdAt: Instant
}

data class OrderCreatedEvent(
    override val orderId: OrderId,
    val userId: UserId,
    override val createdAt: Instant
) : OrderEvent {
    companion object {
        fun from(order: Order): OrderCreatedEvent =
             OrderCreatedEvent(
                orderId = order.id(),
                userId = order.userId,
                createdAt = order.createdAt
            )
    }
}

data class OrderStockPlacedEvent(
    override val orderId: OrderId,
    val productId: ProductId,
    val quantity: Int,
    val unitPrice: BigDecimal,
    override val createdAt: Instant
) : OrderEvent {
    companion object {
        fun from(orderProduct: OrderProduct): OrderStockPlacedEvent =
            OrderStockPlacedEvent(
                orderId = orderProduct.orderId(),
                productId = orderProduct.productId,
                quantity = orderProduct.quantity,
                unitPrice = orderProduct.unitPrice,
                createdAt = orderProduct.createdAt
            )
    }
}

data class OrderCouponAppliedEvent(
    override val orderId: OrderId,
    val couponId: CouponId,
    val discCountAmount: BigDecimal,
    val totalAmount: BigDecimal,
    override val createdAt: Instant
) : OrderEvent {
    companion object {
        fun from(order: Order): OrderCouponAppliedEvent {
            val couponId = order.couponId ?: throw InvalidOrderCouponException(
                orderId = order.id(),
                detail = "주문 쿠폰 적용 이벤트 생성을 위해서는, 쿠폰이 적용된 주문이어야 합니다."
            )
            return OrderCouponAppliedEvent(
                orderId = order.id(),
                couponId = couponId,
                discCountAmount = order.discountAmount,
                totalAmount = order.totalAmount,
                createdAt = order.updatedAt
            )
        }
    }
}

data class OrderCompletedEvent(
    override val orderId: OrderId,
    val order: OrderView,
    override val createdAt: Instant
) : OrderEvent {
    companion object {
        fun from(order: Order): OrderCompletedEvent =
            OrderCompletedEvent(
                orderId = order.id(),
                order = OrderView.from(order),
                createdAt = order.updatedAt
            )
    }
}

data class OrderFailedEvent(
    override val orderId: OrderId,
    val order: OrderView,
    override val createdAt: Instant
) : OrderEvent {
    companion object {
        fun from(order: Order) =
            OrderFailedEvent(
                orderId = order.id(),
                order = OrderView.from(order),
                createdAt = order.updatedAt
            )
    }
}

data class OrderMarkedFailedHandledEvent(
    override val orderId: OrderId,
    override val createdAt: Instant
) : OrderEvent {
    companion object {
        fun from(order: Order) =
            OrderMarkedFailedHandledEvent(
                orderId = order.id(),
                createdAt = order.updatedAt
            )
    }
}
