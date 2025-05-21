package kr.hhplus.be.server.infra.product.persistence

import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductJpaRepository : JpaRepository<Product, Long> {
    fun findAllByIdIn(ids: List<Long>): List<Product>
    fun findAllByStatusOrderByCreatedAtDesc(status: ProductStatus, pageable: Pageable): Page<Product>
}
