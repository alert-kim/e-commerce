package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.product.ProductId
import java.math.BigDecimal
import java.time.Instant

class OrderProduct(
    val orderId: OrderId,
    val productId: ProductId,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal,
    val createdAt: Instant,
)
