package kr.hhplus.be.server.interfaces.product.response

import kr.hhplus.be.server.application.product.result.ProductsResult
import kr.hhplus.be.server.interfaces.common.ServerApiResponse

class ProductsResponse(
    val products: List<ProductResponse>
) : ServerApiResponse {
    companion object {
        fun from(result: ProductsResult): ProductsResponse = when (result) {
            is ProductsResult.Paged -> result.value.content.map { ProductResponse.from(it) }
            is ProductsResult.Listed -> result.value.map { ProductResponse.from(it) }
        }.let { ProductsResponse(it) }
    }
}
