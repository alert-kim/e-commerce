package kr.hhplus.be.server.domain.stock

import kr.hhplus.be.server.common.cache.CacheNames
import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.stock.command.AllocateStockCommand
import kr.hhplus.be.server.domain.stock.command.RestoreStockCommand
import kr.hhplus.be.server.domain.stock.exception.NotFoundStockException
import kr.hhplus.be.server.domain.stock.result.AllocatedStock
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Service
class StockService(
    private val cacheReader: StockCacheReader,
    private val repository: StockRepository
) {
    @Transactional
    @CacheEvict(value = [CacheNames.STOCK_BY_PRODUCT], key = "#command.productId.value")
    fun allocate(command: AllocateStockCommand): AllocatedStock {
        val stock = repository.findByProductId(command.productId)
            ?: throw NotFoundStockException(command.productId)

        return stock.allocate(command.quantity)
    }

    @Transactional
    @CacheEvict(value = [CacheNames.STOCK_BY_PRODUCT], key = "#command.productId.value")
    fun restore(command: RestoreStockCommand) {
        val stock = repository.findByProductId(command.productId)
            ?: throw NotFoundStockException(command.productId)

        stock.restore(command.quantity)
    }

    fun getStocks(productIds: List<ProductId>): List<StockView> =
        productIds.mapNotNull {
            cacheReader.getOrNullByProductId(it)
        }
}
