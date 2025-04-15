package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.product.ProductId
import java.math.BigDecimal
import java.time.Instant

data class OrderProductQueryModel (
    val orderId: OrderId,
    val productId: ProductId,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal,
    val createdAt: Instant,
) {
    companion object {
        fun from(orderProduct: OrderProduct): OrderProductQueryModel =
             OrderProductQueryModel(
                orderId = orderProduct.orderId,
                productId = orderProduct.productId,
                quantity = orderProduct.quantity,
                unitPrice = orderProduct.unitPrice,
                totalPrice = orderProduct.totalPrice,
                createdAt = orderProduct.createdAt
            )
    }
}
