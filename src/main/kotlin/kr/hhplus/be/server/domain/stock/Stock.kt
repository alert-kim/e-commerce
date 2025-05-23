package kr.hhplus.be.server.domain.stock

import jakarta.persistence.*
import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.stock.exception.InvalidStockQuantityToAllocateException
import kr.hhplus.be.server.domain.stock.exception.OutOfStockException
import kr.hhplus.be.server.domain.stock.exception.RequiredStockIdException
import kr.hhplus.be.server.domain.stock.result.AllocatedStock
import java.time.Instant

@Entity
@Table(
    name = "stocks",
    indexes = [
        Index(name = "stock_unq_product", columnList = "product_id", unique = true),
    ]
)
class Stock(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null,
    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "product_id"))
    @Column(nullable = false)
    val productId: ProductId,
    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),
    quantity: Int,
    updatedAt: Instant = Instant.now(),
) {
    @Column(nullable = false)
    var quantity: Int = quantity
        private set

    @Column(nullable = false)
    var updatedAt: Instant = updatedAt
        private set

    fun id(): StockId {
        return id?.let { StockId(it) } ?: throw RequiredStockIdException()
    }

    fun allocate(quantity: Int): AllocatedStock {
        when {
            quantity <= 0 -> throw InvalidStockQuantityToAllocateException("할당할 ${quantity}가 0이하 입니다.")
            this.quantity < quantity -> throw OutOfStockException(productId, quantity, this.quantity)
        }

        this.quantity -= quantity
        this.updatedAt = Instant.now()

        return AllocatedStock(
            productId = productId,
            quantity = quantity,
        )
    }

    fun restore(quantity: Int) {
        if (quantity <= 0) throw InvalidStockQuantityToAllocateException("복구할 ${quantity}가 0이하 입니다.")

        this.quantity += quantity
        this.updatedAt = Instant.now()
    }
}
