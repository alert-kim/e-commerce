package kr.hhplus.be.server.domain.product

import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "product_daily_sale_stats")
class ProductDailySaleStat(
    @EmbeddedId
    val id: ProductDailySaleStatId,
    val createdAt: Instant,
    quantity: Int,
    updatedAt: Instant,
) {
    var quantity: Int = quantity
        private set

    var updatedAt: Instant = updatedAt
        private set

    val productId: ProductId
        get() = id.productId

    val date: LocalDate
        get() = id.date
}
