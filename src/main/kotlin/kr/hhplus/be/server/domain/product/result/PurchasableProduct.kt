package kr.hhplus.be.server.domain.product.result

import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.product.ProductPrice

data class PurchasableProduct(
    val id: ProductId,
    val price: ProductPrice,
)
