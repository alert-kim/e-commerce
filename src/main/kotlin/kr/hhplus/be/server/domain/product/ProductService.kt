package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.domain.common.createPageRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ProductService(
    private val repository: ProductRepository,
) {
    fun getAllByStatusOnPaged(status: ProductStatus, page: Int, pageSize: Int): Page<ProductView> {
        val pageable =
            createPageRequest(page = page, pageSize = pageSize, sort = Sort.by(Sort.Direction.DESC, "createdAt"))
        return repository.findAllByStatusOrderByCreatedAtDesc(status, pageable)
            .map { ProductView.from(it) }
    }

    fun getAllByIds(ids: List<Long>): ProductsView =
        repository.findAllByIds(ids).map { ProductView.from(it) }.let { ProductsView(it) }

}

