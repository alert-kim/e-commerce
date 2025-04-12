package kr.hhplus.be.server.domain.product

import org.springframework.data.domain.Page
import org.springframework.stereotype.Service

@Service
class ProductService {
    fun getAllOnSalePaged(page: Int, size: Int): Page<ProductQueryModel> {
        TODO("Not yet implemented")
    }

}
