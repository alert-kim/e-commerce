package kr.hhplus.be.server.interfaces.product.response

import kr.hhplus.be.server.application.product.result.GetProductsFacadeResult
import kr.hhplus.be.server.interfaces.common.ServerApiResponse

class ProductsPageResponse(
    val totalCount: Long,
    val page: Int,
    val pageSize: Int,
    val products: List<ProductResponse>
) : ServerApiResponse {
    companion object {
        fun from(
            products: GetProductsFacadeResult.Paged,
        ) = ProductsPageResponse(
            totalCount = products.value.totalElements,
            page = products.value.number,
            pageSize = products.value.size,
            products = products.value.content.map { product ->
                ProductResponse.from(product)
            }
        )
    }
}
