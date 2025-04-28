package kr.hhplus.be.server.application.order.command

import kr.hhplus.be.server.domain.order.exception.InvalidOrderPriceException
import kr.hhplus.be.server.domain.order.exception.InvalidOrderProductQuantityException
import kr.hhplus.be.server.domain.product.ProductId
import java.math.BigDecimal

data class OrderFacadeCommand(
    val userId: Long,
    val productsToOrder: List<ProductToOrder>,
    val couponId: Long? = null,
    val originalAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val totalAmount: BigDecimal,
) {
    fun orderProductIds() = productsToOrder.map { it.productId }

    fun quantityOfProduct(productId: ProductId): Int =
        productsToOrder.first { it.productId == productId.value }.quantity

    fun validate() {
        if (productsToOrder.isEmpty()) throw InvalidOrderProductQuantityException("주문 상품 없이 주문 할 수 없습니다.")
        if (productsToOrder.sumOf { it.totalPrice }.compareTo(originalAmount) != 0) throw InvalidOrderPriceException(detail = "주문 상품 단가의 합계와 주문 금액이 일치하지 않습니다.")
        if (originalAmount.compareTo(discountAmount.plus(totalAmount)) != 0) throw InvalidOrderPriceException(detail = "주문 금액과 할인 금액, 총 금액이 일치하지 않습니다.")
        productsToOrder.forEach { it.validate() }
    }

    data class ProductToOrder(
        val productId: Long,
        val quantity: Int,
        val unitPrice: BigDecimal,
        val totalPrice: BigDecimal,
    ) {
        fun validate() {
            if (quantity <= 0) throw InvalidOrderProductQuantityException("상품 수량은 0보다 커야 합니다.")
            if (unitPrice < BigDecimal.ZERO) throw InvalidOrderPriceException(detail = "주문 상품 단가가 0보다 작습니다.")
            if (totalPrice.compareTo(unitPrice.multiply(BigDecimal(quantity))) != 0) {
                throw InvalidOrderPriceException(detail = "주문 상품 총 가격이 단가 * 수량과 일치하지 않습니다.")
            }
        }
    }
}
