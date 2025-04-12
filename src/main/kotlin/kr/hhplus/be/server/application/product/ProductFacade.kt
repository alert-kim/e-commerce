package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.product.ProductQueryModel
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.product.ProductStatus
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service

@Service
class ProductFacade(
    private val service: ProductService,
) {
    fun getAllOnSalePaged(page: Int, pageSize: Int): Page<ProductQueryModel> {
        val products = service.getAllByStatusOnPaged(status = ProductStatus.ON_SALE, page = page, pageSize = pageSize)
        return products.map { product ->
            ProductQueryModel.from(product)
        }
    }
}
