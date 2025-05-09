package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.common.cache.CacheNames
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class ProductCacheReader(
    private val repository: ProductRepository,
) {
    @Cacheable(value = [CacheNames.PRODUCT], key = "#id", unless = "#result == null")
    fun getOrNull(id: Long): ProductView? =
        repository.findById(id)?.let {
            ProductView.from(it)
        }
}
