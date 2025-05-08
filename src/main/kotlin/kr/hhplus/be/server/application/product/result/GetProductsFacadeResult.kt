package kr.hhplus.be.server.application.product.result

import kr.hhplus.be.server.domain.product.ProductView
import kr.hhplus.be.server.domain.stock.StockView
import org.springframework.data.domain.Page

sealed class GetProductsFacadeResult {
    data class ProductWithStock(
        val product: ProductView,
        val stockQuantity: Int
    )

    data class Paged(
        val value: Page<ProductWithStock>
    ) {
        companion object {
            fun from(products: Page<ProductView>, stocks: List<StockView>): Paged =
                products.map { product ->
                    val stock = stocks.first { it.productId == product.id }
                    ProductWithStock(
                        product = product,
                        stockQuantity = stock.quantity
                    )
                }.let { Paged(it) }
        }
    }

    data class Listed(
        val value: List<ProductWithStock>
    ) {
        companion object {
            fun from(products: List<ProductView>, stocks: List<StockView>): Listed =
                products.map { product ->
                    val stock = stocks.first { it.productId == product.id }
                    ProductWithStock(
                        product = product,
                        stockQuantity = stock.quantity
                    )
                }.let { Listed(it) }
        }
    }
}
