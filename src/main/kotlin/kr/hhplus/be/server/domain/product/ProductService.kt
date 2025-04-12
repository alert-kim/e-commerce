package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.domain.common.createPageRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val repository: ProductRepository,
) {
    fun getAllByStatusOnPaged(status: ProductStatus, page: Int, pageSize: Int): Page<Product> {
        val pageable =
            createPageRequest(page = page, pageSize = pageSize, sort = Sort.by(Sort.Direction.DESC, "createdAt"))
        return repository.findAllByStatus(status, pageable)
    }
}
