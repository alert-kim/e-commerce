package kr.hhplus.be.server.domain.product

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProductRepository {
    fun findAllByIds(ids: List<Long>): List<Product>
    fun findAllByStatusOrderByCreatedAtDesc(status: ProductStatus, pageable: Pageable): Page<Product>
}
