package kr.hhplus.be.server.interfaces.product.response

import kr.hhplus.be.server.domain.product.PopularProductsView
import kr.hhplus.be.server.interfaces.common.ServerApiResponse

class ProductsResponse(
    val products: List<ProductResponse>
) : ServerApiResponse {
    companion object {
        fun from(
            popularProducts: PopularProductsView
        ) = ProductsResponse(
            products = popularProducts.products.map { product ->
                ProductResponse.from(product)
            },
        )
    }
}
