package kr.hhplus.be.server.interfaces.product.response

import kr.hhplus.be.server.domain.product.ProductQueryModel
import kr.hhplus.be.server.interfaces.common.ServerApiResponse
import org.springframework.data.domain.Page

class ProductsPageResponse(
    val totalCount: Long,
    val page: Int,
    val pageSize: Int,
    val products: List<ProductResponse>
) : ServerApiResponse {
    companion object {
        fun from(
            products: Page<ProductQueryModel>,
        ) = ProductsPageResponse(
                totalCount = products.totalElements,
                page = products.number,
                pageSize = products.size,
                products = products.content.map { product ->
                    ProductResponse.from(product)
                }
            )

    }
}
