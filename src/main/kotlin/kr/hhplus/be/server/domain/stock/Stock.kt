package kr.hhplus.be.server.domain.stock

import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.stock.exception.InvalidStockQuantityToAllocateException
import kr.hhplus.be.server.domain.stock.exception.OutOfStockException
import kr.hhplus.be.server.domain.stock.exception.RequiredStockIdException
import kr.hhplus.be.server.domain.stock.result.AllocatedStock
import java.time.Instant

class Stock(
    protected val id: Long? = null,
    val productId: ProductId,
    quantity: Int,
    val createdAt: Instant = Instant.now(),
    updatedAt: Instant = Instant.now(),
) {
    var quantity: Int = quantity
        private set

    var updatedAt: Instant = updatedAt
        private set

    fun id(): StockId {
        return id?.let { StockId(it) } ?: throw RequiredStockIdException()
    }

    fun allocate(quantity: Int): AllocatedStock {
        when {
            quantity <= 0 -> throw InvalidStockQuantityToAllocateException("${quantity}가 0이하 입니다.")
            this.quantity < quantity -> throw OutOfStockException(productId, quantity, this.quantity)
        }

        this.quantity -= quantity
        this.updatedAt = Instant.now()

        return AllocatedStock(
            productId = productId,
            quantity = quantity,
        )
    }
}
