package kr.hhplus.be.server.infra.stock

import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.stock.Stock
import kr.hhplus.be.server.domain.stock.StockRepository
import org.springframework.stereotype.Repository

@Repository
class StockRepositoryImpl : StockRepository {
    override fun findByProductId(productId: ProductId): Stock? {
        TODO("Not yet implemented")
    }

    override fun findAllByProductIds(productIds: Collection<ProductId>): List<Stock> {
        TODO("Not yet implemented")
    }

    override fun save(stock: Stock): Stock {
        // TODO: Implement database query to save stock
        return stock
    }
}
