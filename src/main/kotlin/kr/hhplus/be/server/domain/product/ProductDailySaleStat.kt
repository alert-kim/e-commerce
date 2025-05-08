package kr.hhplus.be.server.domain.product

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "product_daily_sale_stats")
class ProductDailySaleStat(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val productId: ProductId,
    val date: LocalDate,
    val createdAt: Instant,
    quantity: Int,
    updatedAt: Instant,
) {
    var quantity: Int = quantity
        private set

    var updatedAt: Instant = updatedAt
        private set
}
