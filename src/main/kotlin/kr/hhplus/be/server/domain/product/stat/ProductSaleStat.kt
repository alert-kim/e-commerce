package kr.hhplus.be.server.domain.product.stat

import jakarta.persistence.*
import kr.hhplus.be.server.common.util.TimeZone
import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.product.excpetion.RequiredProductStatIdException
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(
    name = "product_sale_stats",
    indexes = [
        Index(name = "product_sale_stats_idx_date_product", columnList = "date, productId")
    ]
)
class ProductSaleStat(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null,
    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "product_id"))
    val productId: ProductId,
    val quantity: Int,
    val date: LocalDate,
    val createdAt: Instant,
) {

    fun id(): ProductSaleStatId = id?.let { ProductSaleStatId(it) }
        ?: throw RequiredProductStatIdException()

    companion object {
        fun new(productId: ProductId, quantity: Int): ProductSaleStat {
            val now = Instant.now()
            return ProductSaleStat(
                productId = productId,
                quantity = quantity,
                date = now.atZone(TimeZone.KSTId).toLocalDate(),
                createdAt = now
            )
        }
    }
}
