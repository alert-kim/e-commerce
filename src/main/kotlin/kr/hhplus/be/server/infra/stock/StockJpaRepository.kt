package kr.hhplus.be.server.infra.stock

import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.stock.Stock
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StockJpaRepository : JpaRepository<Stock, Long> {
    fun findByProductId(productId: ProductId): Stock?

    fun findAllByProductIdIn(productIds: Collection<ProductId>): List<Stock>
}
