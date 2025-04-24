package kr.hhplus.be.server.application.product.result

import kr.hhplus.be.server.domain.product.ProductView
import org.springframework.data.domain.Page

sealed class ProductsResult {
    data class Paged(val value: Page<ProductView>) : ProductsResult()
    data class Listed(val value: List<ProductView>) : ProductsResult()
}
