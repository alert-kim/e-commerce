package kr.hhplus.be.server.domain.product.result

import kr.hhplus.be.server.domain.product.ProductStockAllocated

data class AllocatedStockResult (
    val stocks: List<ProductStockAllocated>
)
