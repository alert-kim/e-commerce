package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.domain.common.createPageRequest
import kr.hhplus.be.server.domain.product.excpetion.NotFoundProductException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ProductService(
    private val cacheReader: ProductCacheReader,
    private val repository: ProductRepository,
) {
    fun getAllByStatusOnPaged(status: ProductStatus, page: Int, pageSize: Int): Page<ProductView> {
        val pageable =
            createPageRequest(page = page, pageSize = pageSize, sort = Sort.by(Sort.Direction.DESC, "createdAt"))
        return repository.findAllByStatusOrderByCreatedAtDesc(status, pageable)
            .map { ProductView.from(it) }
    }

    fun get(id: Long): ProductView =
        cacheReader.getOrNull(id) ?: throw NotFoundProductException("by id($id)")

    fun getAllByIds(ids: List<Long>): ProductsView {
        val products = ids.mapNotNull { id -> cacheReader.getOrNull(id) }
        return ProductsView(products)
    }
}
