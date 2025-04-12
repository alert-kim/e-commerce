package kr.hhplus.be.server.domain.product

import java.time.Instant

class ProductStock(
    val productId: ProductId,
    val createdAt: Instant,
    quantity: Long,
    updatedAt: Instant,
) {
    var quantity: Long = quantity
        private set

    var updatedAt: Instant = updatedAt
        private set
}
