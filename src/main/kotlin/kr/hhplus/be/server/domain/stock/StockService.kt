package kr.hhplus.be.server.domain.stock

import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.stock.command.AllocateStockCommand
import kr.hhplus.be.server.domain.stock.exception.NotFoundStockException
import kr.hhplus.be.server.domain.stock.result.AllocatedStock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Service
class StockService(
    private val repository: StockRepository
) {
    @Transactional
    fun allocate(command: AllocateStockCommand): AllocatedStock {
        val stock = repository.findByProductId(command.productId)
            ?: throw NotFoundStockException(command.productId)

        return stock.allocate(command.quantity)
    }

    fun getStocks(productIds: List<ProductId>): List<StockView> {
        val stocks = repository.findAllByProductIds(productIds)
        return stocks.map { StockView.from(it) }
    }

}
