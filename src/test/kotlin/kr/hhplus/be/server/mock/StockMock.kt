package kr.hhplus.be.server.mock

import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.stock.result.AllocatedStock

object StockMock {

    fun allocated(
        productId: ProductId = ProductMock.id(),
        quantity: Int = 10,
    ): AllocatedStock = AllocatedStock(
        productId = productId,
        quantity = quantity,
    )
}
