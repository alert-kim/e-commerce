package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.order.exception.InvalidOrderCouponException
import kr.hhplus.be.server.domain.order.exception.InvalidOrderPriceException
import kr.hhplus.be.server.domain.product.ProductStockAllocated
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal

data class OrderSheet(
    val orderId: OrderId,
    val userId: UserId,
    val orderProducts: List<OrderProduct>,
    val couponId: Long? = null,
    val originalAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val totalAmount: BigDecimal,
) {
    data class OrderProduct(
        val productId: Long,
        val quantity: Int,
        val unitPrice: BigDecimal,
        val totalPrice: BigDecimal,
    )

    fun verifyProductPrice(stocks: List<ProductStockAllocated>) {
        orderProducts.forEachIndexed { index, orderProduct ->
            val stock = stocks[index]
            require(stock.productId.value == orderProduct.productId)
            require(stock.quantity == orderProduct.quantity)

            if (orderProduct.unitPrice.compareTo(stock.unitPrice) != 0) {
                throw InvalidOrderPriceException(
                    stock.productId,
                    "wrong unit price, expected: ${orderProduct.unitPrice}, actual: ${stock.unitPrice}"
                )
            }

            val totalPrice = orderProduct.unitPrice.multiply(BigDecimal(orderProduct.quantity))
            if (totalPrice.compareTo(orderProduct.totalPrice) != 0) {
                throw InvalidOrderPriceException(
                    stock.productId,
                    "wrong total price, expected: ${orderProduct.totalPrice}, actual: ${totalPrice}"
                )
            }
        }
    }

    fun verifyCoupon(coupon: Coupon) {
        val couponId = coupon.requireId().value
        if (this.couponId != couponId) {
            throw InvalidOrderCouponException(
                orderId,
                "wrong coupon id, expected: $couponId, actual: ${this.couponId}"
            )
        }
    }
}
