package kr.hhplus.be.server.interfaces.order.response

import kr.hhplus.be.server.domain.order.OrderProductQueryModel
import kr.hhplus.be.server.domain.order.OrderQueryModel
import kr.hhplus.be.server.domain.order.OrderStatus
import kr.hhplus.be.server.interfaces.common.ServerApiResponse
import java.math.BigDecimal
import java.time.Instant

data class OrderResponse(
    val id: Long,
    val userId: Long,
    val status: OrderStatus,
    val couponId: Long?,
    val originalAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val totalAmount: BigDecimal,
    val orderProducts: List<OrderProductResponse>,
    val createdAt: Instant,
): ServerApiResponse {
    data class OrderProductResponse(
        val productId: Long,
        val quantity: Int,
        val unitPrice: BigDecimal,
        val totalPrice: BigDecimal,
    ) {
        companion object {
            fun from(
                orderProduct: OrderProductQueryModel,
            ): OrderProductResponse =
                with(orderProduct) {
                    OrderProductResponse(
                        productId = productId.value,
                        quantity = quantity,
                        unitPrice = unitPrice,
                        totalPrice = totalPrice,
                    )
                }
        }
    }

    companion object {
        fun from(
            order: OrderQueryModel,
        ): OrderResponse =
            with(order) {
                OrderResponse(
                    id = id.value,
                    userId = userId.value,
                    status = status,
                    couponId = couponId?.value,
                    originalAmount = originalAmount,
                    discountAmount = discountAmount,
                    totalAmount = totalAmount,
                    orderProducts = order.products.map { OrderProductResponse.from(it) },
                    createdAt = createdAt,
                )
            }
    }
}
