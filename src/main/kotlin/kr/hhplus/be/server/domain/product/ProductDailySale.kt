package kr.hhplus.be.server.domain.product

import java.time.Instant
import java.time.LocalDate

class ProductDailySale(
    val date: LocalDate,
    val productId: ProductId,
    val createdAt: Instant,
    quantity: Int,
    updatedAt: Instant,
) {
    var quantity: Int = quantity
        private set

    var updatedAt: Instant = updatedAt
        private set

    fun addQuantity(quantity: Int) {
        this.quantity += quantity
        this.updatedAt = Instant.now()
    }

    companion object {
        fun new(
            date: LocalDate,
            productId: ProductId,
            quantity: Int,
        ): ProductDailySale {
            return ProductDailySale(
                date = date,
                productId = productId,
                quantity = quantity,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )
        }
    }
}
