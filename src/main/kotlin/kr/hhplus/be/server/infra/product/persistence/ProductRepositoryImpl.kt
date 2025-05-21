package kr.hhplus.be.server.infra.product.persistence

import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductRepository
import kr.hhplus.be.server.domain.product.ProductStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class ProductRepositoryImpl(
    private val jpaRepository: ProductJpaRepository
) : ProductRepository {
    override fun findById(id: Long): Product? =
        jpaRepository.findById(id).orElse(null)

    override fun findAllByIds(ids: List<Long>): List<Product> =
        jpaRepository.findAllByIdIn(ids)

    override fun findAllByStatusOrderByCreatedAtDesc(status: ProductStatus, pageable: Pageable): Page<Product> =
        jpaRepository.findAllByStatusOrderByCreatedAtDesc(status, pageable)
}
