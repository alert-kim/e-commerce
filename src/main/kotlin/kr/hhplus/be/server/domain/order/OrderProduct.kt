package kr.hhplus.be.server.domain.order

import jakarta.persistence.*
import kr.hhplus.be.server.domain.order.exception.RequiredOrderIdException
import kr.hhplus.be.server.domain.order.exception.RequiredOrderProductIdException
import kr.hhplus.be.server.domain.product.ProductId
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "order_products")
class OrderProduct(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    protected var order: Order? = null,

    val productId: ProductId,
    val quantity: Int,

    @Column(precision = 20, scale = 2)
    val unitPrice: BigDecimal,

    @Column(precision = 20, scale = 2)
    val totalPrice: BigDecimal,

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
