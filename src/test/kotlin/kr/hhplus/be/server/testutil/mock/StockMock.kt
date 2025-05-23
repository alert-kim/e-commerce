package kr.hhplus.be.server.testutil.mock

import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.stock.Stock
import kr.hhplus.be.server.domain.stock.StockId
import kr.hhplus.be.server.domain.stock.StockView
import kr.hhplus.be.server.domain.stock.result.AllocatedStock
import java.time.Instant

object StockMock {
    fun id(
        id: Long = IdMock.value(),
    ): StockId = StockId(id)

    fun stock(
        id: StockId? = StockId(IdMock.value()),
        productId: ProductId = ProductMock.id(),
        quantity: Int = 100,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ): Stock = Stock(
        id = id?.value,
        productId = productId,
        quantity = quantity,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun view(
        id: StockId = StockId(IdMock.value()),
        productId: ProductId = ProductMock.id(),
        quantity: Int = 100,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ): StockView = StockView(
        id = id,
        productId = productId,
        quantity = quantity,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun allocated(
        productId: ProductId = ProductMock.id(),
        quantity: Int = 10,
    ): AllocatedStock = AllocatedStock(
        productId = productId,
        quantity = quantity,
    )
}
