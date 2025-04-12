package kr.hhplus.be.server.domain.product

import java.math.BigDecimal
import java.time.Instant

data class ProductQueryModel(
    val id: ProductId,
    val status: ProductStatus,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val stock: Long,
    val createdAt: Instant,
)
