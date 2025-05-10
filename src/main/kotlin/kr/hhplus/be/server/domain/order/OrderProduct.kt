package kr.hhplus.be.server.domain.order

import jakarta.persistence.AttributeOverride
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import kr.hhplus.be.server.domain.order.exception.RequiredOrderIdException
import kr.hhplus.be.server.domain.order.exception.RequiredOrderProductIdException
import kr.hhplus.be.server.domain.product.ProductId
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(
    name = "order_products",
    indexes = [
        Index(name = "order_product_idx_order_id", columnList = "order_id"),
    ]
)
class OrderProduct(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private var order: Order? = null,
    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "product_id"))
    @Column(nullable = false)
    val productId: ProductId,
    @Column(nullable = false)
    val quantity: Int,
    @Column(precision = 20, scale = 2, nullable = false)
    val unitPrice: BigDecimal,
    @Column(precision = 20, scale = 2, nullable = false)
    val totalPrice: BigDecimal,
    @Column(nullable = false)
    val createdAt: Instant,
) {
    fun id(): OrderProductId =
        id?.let { OrderProductId(it) } ?: throw RequiredOrderProductIdException()

    fun orderId(): OrderId =
        order?.id() ?: throw RequiredOrderIdException()

    companion object {
        fun new(
            order: Order,
            productId: ProductId,
            quantity: Int,
            unitPrice: BigDecimal,
        ): OrderProduct =
            OrderProduct(
                order = order,
                productId = productId,
                quantity = quantity,
                unitPrice = unitPrice,
                totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity.toLong())),
                createdAt = Instant.now(),
            )
    }
}
