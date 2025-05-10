package kr.hhplus.be.server.domain.product.stat

import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.*
import kr.hhplus.be.server.domain.product.ProductId
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(
    name = "product_daily_sale_stats",
    indexes = [
        Index(name = "product_daily_sale_stat_idx_date", columnList = "date"),
    ]
)
class ProductDailySaleStat(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "product_id"))
    @Column(nullable = false)
    val productId: ProductId,
    @Column(nullable = false)
    val date: LocalDate,
    @Column(nullable = false)
    val createdAt: Instant,
    quantity: Int,
    updatedAt: Instant,
) {
    @Column(nullable = false)
    var quantity: Int = quantity
        private set

    @Column(nullable = false)
    var updatedAt: Instant = updatedAt
        private set
}
