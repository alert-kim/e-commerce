package kr.hhplus.be.server.domain.stock

import kr.hhplus.be.server.domain.product.ProductId

interface StockRepository {
    fun findAllByProductIds(productIds: Collection<ProductId>): List<Stock>
    fun save(stock: Stock): Stock
}
