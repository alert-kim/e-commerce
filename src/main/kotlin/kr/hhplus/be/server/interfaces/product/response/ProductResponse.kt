package kr.hhplus.be.server.interfaces.product.response

import kr.hhplus.be.server.domain.product.ProductStatus
import java.time.Instant

class ProductResponse(
    val id: Long,
    val status: ProductStatus,
    val name: String,
    val description: String,
    val price: Long,
    val stock: Long,
    val createdAt: Instant,
)
