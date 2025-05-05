package kr.hhplus.be.server.application.order.command

import kr.hhplus.be.server.domain.product.ProductId

data class RestoreStockOrderProductProcessorCommand(
    val productId: ProductId,
    val quantity: Int
)
