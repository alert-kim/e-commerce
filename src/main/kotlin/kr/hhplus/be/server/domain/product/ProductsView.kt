package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.domain.product.excpetion.NotFoundProductException
import kr.hhplus.be.server.domain.product.result.PurchasableProduct
import java.math.BigDecimal

data class ProductsView(
    val value: List<ProductView>
) {
    fun validatePurchasable(
        productId: Long,
        price: BigDecimal,
    ): PurchasableProduct {
        val product = value.find { it.id.value == productId } ?: throw NotFoundProductException("by id: $productId")
        return product.validatePurchasable(price)
    }

    companion object {
        fun from(products: List<Product>): ProductsView {
            return ProductsView(products.map { ProductView.from(it) })
        }
    }

}
