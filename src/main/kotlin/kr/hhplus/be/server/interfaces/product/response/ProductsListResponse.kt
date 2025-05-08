package kr.hhplus.be.server.interfaces.product.response

import kr.hhplus.be.server.application.product.result.GetProductsFacadeResult
import kr.hhplus.be.server.interfaces.common.ServerApiResponse

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
