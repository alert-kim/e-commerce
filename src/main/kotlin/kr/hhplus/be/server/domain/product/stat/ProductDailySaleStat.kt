package kr.hhplus.be.server.domain.product.stat

import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "product_daily_sale_stats")
class ProductDailySaleStat(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "product_id"))
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
