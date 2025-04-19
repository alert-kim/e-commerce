package kr.hhplus.be.server.interfaces.product.response

import kr.hhplus.be.server.domain.product.PopularProductsQueryModel
import kr.hhplus.be.server.domain.product.ProductQueryModel
import kr.hhplus.be.server.interfaces.common.ServerApiResponse

class ProductsResponse(
    val products: List<ProductResponse>
) : ServerApiResponse {
    companion object {
        fun from(
            popularProducts: PopularProductsQueryModel
        ) = ProductsResponse(
            products = popularProducts.products.map { product ->
                ProductResponse.from(product)
            },
        )
    }
}
