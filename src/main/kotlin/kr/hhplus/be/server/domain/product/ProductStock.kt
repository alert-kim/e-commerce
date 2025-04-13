package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.domain.product.excpetion.OutOfStockProductException
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

    fun allocate(quantity: Int) {
        if (quantity > this.quantity) {
            throw OutOfStockProductException(
                productId = productId.value,
                required = quantity,
                remaining = this.quantity,
            )
        }
        this.quantity -= quantity
    }
}
