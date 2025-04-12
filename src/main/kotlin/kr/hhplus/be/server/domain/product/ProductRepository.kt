package kr.hhplus.be.server.domain.product

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProductRepository {
    fun findAllByStatus(status: ProductStatus, pageable: Pageable): Page<Product>
}
