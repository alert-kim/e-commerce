package kr.hhplus.be.server.infra.product

import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductRepository
import kr.hhplus.be.server.domain.product.ProductStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class ProductRepositoryImpl : ProductRepository {
    override fun save(product: Product) {
        TODO("Not yet implemented")
    }

    override fun findAllByIds(ids: List<Long>): List<Product> {
        TODO("Not yet implemented")
    }

    override fun findAllByStatus(status: ProductStatus, pageable: Pageable): Page<Product> {
        TODO("Not yet implemented")
    }
}
