package kr.hhplus.be.server.domain.stock

import kr.hhplus.be.server.common.cache.CacheNames
import kr.hhplus.be.server.domain.product.ProductId
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class StockCacheReader(
    private val repository: StockRepository
) {
    @Cacheable(value = [CacheNames.STOCK_BY_PRODUCT], key = "#productId", unless = "#result == null")
    fun getOrNullByProductId(productId: ProductId): StockView? =
        repository.findByProductId(productId)?.let { StockView.from(it) }
}
