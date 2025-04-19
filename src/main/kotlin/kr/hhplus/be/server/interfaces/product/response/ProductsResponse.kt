package kr.hhplus.be.server.interfaces.product.response

import kr.hhplus.be.server.domain.product.ProductQueryModel
import kr.hhplus.be.server.interfaces.common.ServerApiResponse
import org.springframework.data.domain.Page

class ProductsResponse(
    val products: List<ProductResponse>
) : ServerApiResponse {
    companion object {
        fun from(
            products: List<ProductQueryModel>,
        ) = ProductsResponse(
            products = products.map { product ->
                ProductResponse.from(product)
            },
        )
    }
}
