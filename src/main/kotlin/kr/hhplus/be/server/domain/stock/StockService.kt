package kr.hhplus.be.server.domain.stock

import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.stock.command.AllocateStocksCommand
import kr.hhplus.be.server.domain.stock.exception.NotFoundStockException
import kr.hhplus.be.server.domain.stock.result.AllocatedStock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Service
class StockService(
    private val stockRepository: StockRepository
) {
    fun getStock(productId: ProductId): StockView {
        val stock = stockRepository.findByProductId(productId)
            ?: throw NotFoundStockException(productId)
        return StockView.from(stock)
    }

    fun getStocks(productIds: List<ProductId>): List<StockView> {
        val stocks = stockRepository.findAllByProductIds(productIds)
        return stocks.map { StockView.from(it) }
    }

    @Transactional
    fun allocate(command: AllocateStocksCommand): List<AllocatedStock> {
        val stocksByProductId = stockRepository.findAllByProductIds(command.productIds).associateBy { it.productId }

        return command.needStocks.map { (productId, quantity) ->
            val stock = stocksByProductId[productId] ?: throw NotFoundStockException(productId)
            stock.allocate(quantity)
        }
    }
}
