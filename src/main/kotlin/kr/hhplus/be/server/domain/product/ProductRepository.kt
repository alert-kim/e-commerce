package kr.hhplus.be.server.domain.product

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository {
    fun save(product: Product)
    fun findAllByIds(ids: List<Long>): List<Product>
    fun findAllByStatus(status: ProductStatus, pageable: Pageable): Page<Product>
}
