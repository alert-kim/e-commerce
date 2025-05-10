package kr.hhplus.be.server.domain.product

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import kr.hhplus.be.server.domain.product.excpetion.RequiredProductIdException
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(
    name = "products",
    indexes = [
        Index(name = "products_idx_status", columnList = "status"),
    ]
)
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private val id: Long? = null,
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20)")
    var status: ProductStatus,
    @Column(nullable = false, columnDefinition = "varchar(100)")
    val name: String,
    @Column(nullable = false, columnDefinition = "text")
    val description: String,
    @Column(nullable = false)
    val price: BigDecimal,
    @Column(nullable = false)
    val createdAt: Instant,
    @Column(nullable = false)
    var updatedAt: Instant,
) {

    fun id(): ProductId =
        id?.let { ProductId(it) } ?: throw RequiredProductIdException()
}
