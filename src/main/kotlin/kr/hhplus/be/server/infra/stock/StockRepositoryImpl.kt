package kr.hhplus.be.server.infra.stock

import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.stock.Stock
import kr.hhplus.be.server.domain.stock.StockRepository
import org.springframework.stereotype.Repository

@Repository
class StockRepositoryImpl(
    private val stockJpaRepository: StockJpaRepository
) : StockRepository {
    override fun findByProductId(productId: ProductId): Stock? =
        stockJpaRepository.findByProductId(productId)

    override fun findAllByProductIds(productIds: Collection<ProductId>): List<Stock> =
        stockJpaRepository.findAllByProductIdIn(productIds)

    override fun save(stock: Stock): Stock =
        stockJpaRepository.save(stock)
}
