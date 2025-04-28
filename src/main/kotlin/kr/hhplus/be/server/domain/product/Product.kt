package kr.hhplus.be.server.domain.product

import jakarta.persistence.*
import kr.hhplus.be.server.domain.product.excpetion.RequiredProductIdException
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "products")
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    protected val id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ProductStatus,

    val name: String,
    val description: String,
    val price: BigDecimal,
    val createdAt: Instant,
    var updatedAt: Instant,
) {

    fun id(): ProductId =
        id?.let { ProductId(it) } ?: throw RequiredProductIdException()
}
