package kr.hhplus.be.server.domain.stock

import kr.hhplus.be.server.domain.product.ProductId
import java.time.Instant

data class StockView(
    val id: StockId,
    val productId: ProductId,
    val quantity: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(stock: Stock): StockView {
            return StockView(
                id = stock.id(),
                productId = stock.productId,
                quantity = stock.quantity,
                createdAt = stock.createdAt,
                updatedAt = stock.updatedAt
            )
        }
    }
}
