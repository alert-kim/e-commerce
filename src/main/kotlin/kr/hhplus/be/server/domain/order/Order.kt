package kr.hhplus.be.server.domain.order

import jakarta.persistence.AttributeOverride
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import kr.hhplus.be.server.domain.coupon.CouponId
import kr.hhplus.be.server.domain.coupon.result.UsedCoupon
import kr.hhplus.be.server.domain.order.exception.AlreadyCouponAppliedException
import kr.hhplus.be.server.domain.order.exception.InvalidOrderStatusException
import kr.hhplus.be.server.domain.order.exception.RequiredOrderIdException
import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.product.ProductPrice
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(
    name = "orders",
)
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null,
    @Column(nullable = false)
    val userId: UserId,
    @Column(nullable = false)
    val createdAt: Instant,
    status: OrderStatus,
    originalAmount: BigDecimal,
    discountAmount: BigDecimal,
    totalAmount: BigDecimal,
    orderProducts: List<OrderProduct>,
    couponId: CouponId?,
    updatedAt: Instant,
) {
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20)", nullable = false)
    var status: OrderStatus = status
        private set

    @Column(precision = 20, scale = 2, nullable = false)
    var originalAmount: BigDecimal = originalAmount
        private set

    @Column(precision = 20, scale = 2, nullable = false)
    var discountAmount: BigDecimal = discountAmount
        private set

    @Column(precision = 20, scale = 2, nullable = false)
    var totalAmount: BigDecimal = totalAmount
        private set

    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "coupon_id"))
    var couponId: CouponId? = couponId
        private set

    @Column(nullable = false)
    var updatedAt: Instant = updatedAt
        private set

    val products: List<OrderProduct>
        get() = _products.toList()

    @OneToMany(
        mappedBy = "order",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY,
    )
    private val _products: MutableList<OrderProduct> = orderProducts.toMutableList()

    fun id(): OrderId =
        id?.let { OrderId(it) } ?: throw RequiredOrderIdException()

    fun placeStock(
        productId: ProductId,
        quantity: Int,
        unitPrice: ProductPrice,
    ) {
        if (status != OrderStatus.READY && status != OrderStatus.STOCK_ALLOCATED) {
            throw InvalidOrderStatusException(
                id = id(),
                status = status,
                expect = OrderStatus.READY,
            )
        }
        OrderProduct.new(
            order = this,
            productId = productId,
            quantity = quantity,
            unitPrice = unitPrice.value,
        ).also { orderProduct ->
            addOrderProduct(orderProduct)
        }
        status = OrderStatus.STOCK_ALLOCATED
        updatedAt = Instant.now()
    }

    fun applyCoupon(coupon: UsedCoupon) {
        if (status != OrderStatus.STOCK_ALLOCATED) {
            throw InvalidOrderStatusException(
                id = id(),
                status = status,
                expect = OrderStatus.READY,
            )
        }
        val newCouponId = coupon.id
        when (val originalCouponId = this.couponId) {
            null -> Unit
            newCouponId -> return
            else -> throw AlreadyCouponAppliedException(
                id = id(), couponId = originalCouponId, newCouponId = newCouponId
            )
        }

        this.discountAmount = coupon.calculateDiscountAmount(totalAmount)
        this.totalAmount = totalAmount.minus(discountAmount)
        this.couponId = newCouponId
        this.updatedAt = Instant.now()
    }

    fun pay() {
        if (status != OrderStatus.STOCK_ALLOCATED) {
            throw InvalidOrderStatusException(
                id = id(),
                status = status,
                expect = OrderStatus.STOCK_ALLOCATED,
            )
        }
        this.status = OrderStatus.COMPLETED
        updatedAt = Instant.now()
    }

    fun fail() {
        if (isFailed()) return
        this.status = OrderStatus.FAILED
        updatedAt = Instant.now()
    }

    fun failHandled() {
        if (!isFailed()) {
            throw InvalidOrderStatusException(
                id = id(),
                status = status,
                expect = OrderStatus.FAILED,
            )
        }
        this.status = OrderStatus.FAIL_HANDLED
        updatedAt = Instant.now()
    }

    fun isFailed(): Boolean = status == OrderStatus.FAILED

    private fun addOrderProduct(
        orderProduct: OrderProduct,
    ) {
        _products.add(orderProduct)
        originalAmount = originalAmount.add(orderProduct.totalPrice)
        totalAmount = totalAmount.add(orderProduct.totalPrice)
    }

    companion object {
        fun new(
            userId: UserId,
        ): Order = Order(
            userId = userId,
            status = OrderStatus.READY,
            originalAmount = BigDecimal.ZERO,
            discountAmount = BigDecimal.ZERO,
            totalAmount = BigDecimal.ZERO,
            orderProducts = emptyList(),
            couponId = null,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )
    }
}
