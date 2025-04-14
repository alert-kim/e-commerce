package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.coupon.CouponId
import kr.hhplus.be.server.domain.order.exception.InvalidOrderStatusException
import kr.hhplus.be.server.domain.order.exception.RequiredOrderIdException
import kr.hhplus.be.server.domain.product.ProductStockAllocated
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

class Order(
    val id: OrderId? = null,
    val userId: UserId,
    val createdAt: Instant,
    status: OrderStatus,
    originalAmount: BigDecimal,
    discountAmount: BigDecimal,
    totalAmount: BigDecimal,
    orderProducts: List<OrderProduct>,
    couponId: CouponId?,
    updatedAt: Instant,
) {
    var status: OrderStatus = status
        private set

    var originalAmount: BigDecimal = originalAmount
        private set

    var discountAmount: BigDecimal = discountAmount
        private set

    var totalAmount: BigDecimal = totalAmount
        private set

    var couponId: CouponId? = couponId
        private set

    var updatedAt: Instant = updatedAt
        private set

    var _products: MutableList<OrderProduct> = orderProducts.toMutableList()

    val products: List<OrderProduct>
        get() = _products.toList()

    fun requireId(): OrderId =
        id ?: throw RequiredOrderIdException()

    fun placeStock(stocks: List<ProductStockAllocated>) {
        if (status != OrderStatus.READY) {
            throw InvalidOrderStatusException(
                orderId = requireId().value,
                status = status,
                expect = OrderStatus.READY,
            )
        }
        stocks.forEach { stock ->
            OrderProduct.new(
                orderId = requireId(),
                productId = stock.productId,
                quantity = stock.quantity,
                unitPrice = stock.unitPrice,
            ).also { orderProduct ->
                addOrderProduct(orderProduct)
            }
        }
        status = OrderStatus.STOCK_ALLOCATED
        updatedAt = Instant.now()
    }

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

