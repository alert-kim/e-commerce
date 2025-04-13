package kr.hhplus.be.server.domain.product

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface ProductRepository {
    fun save(product: Product)
    fun findAllByIds(ids: List<Long>): List<Product>
    fun findAllByStatus(status: ProductStatus, pageable: Pageable): Page<Product>
}
