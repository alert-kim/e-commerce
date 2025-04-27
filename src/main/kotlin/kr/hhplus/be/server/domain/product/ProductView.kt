package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.domain.product.excpetion.NotOnSaleProductException
import kr.hhplus.be.server.domain.product.excpetion.ProductPriceMismatchException
import kr.hhplus.be.server.domain.product.result.PurchasableProduct
import java.math.BigDecimal
import java.time.Instant

data class ProductView(
    val id: ProductId,
    val status: ProductStatus,
    val name: String,
    val description: String,
    val price: ProductPrice,
    val createdAt: Instant,
) {
    fun validatePurchasable(
        price: BigDecimal,
    ): PurchasableProduct {
        validateOnSale()
        validatePrice(price)
        return PurchasableProduct(
            id = this.id,
            price = this.price,
        )
    }

    private fun validateOnSale(
    ) {
        if (this.status != ProductStatus.ON_SALE) {
            throw NotOnSaleProductException(this.id.value)
        }
    }

    private fun validatePrice(
        price: BigDecimal,
    ) {
        if (this.price.isEqualTo(price).not()) {
            throw ProductPriceMismatchException(
                id = this.id.value,
                actual = this.price.value,
                expect = price,
            )
        }
    }

    companion object {
        fun from(product: Product): ProductView {
            return ProductView(
                id = product.requireId(),
                status = product.status,
                name = product.name,
                description = product.description,
                price = ProductPrice(product.price),
                createdAt = product.createdAt,
            )
        }
    }
}
