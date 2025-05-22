package kr.hhplus.be.server.interfaces.product.api.response

import kr.hhplus.be.server.application.product.result.GetProductsFacadeResult
import kr.hhplus.be.server.interfaces.common.api.ServerApiResponse

class ProductsListResponse(
    val products: List<ProductResponse>
) : ServerApiResponse {
    companion object {
        fun from(result: GetProductsFacadeResult.Listed): ProductsListResponse =
            ProductsListResponse(
                products = result.value.map { product ->
                    ProductResponse.from(product)
                }
            )
    }
}
