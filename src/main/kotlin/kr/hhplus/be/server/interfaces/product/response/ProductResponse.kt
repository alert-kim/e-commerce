package kr.hhplus.be.server.interfaces.product.response

import kr.hhplus.be.server.domain.product.ProductView
import kr.hhplus.be.server.domain.product.ProductStatus
import java.math.BigDecimal
import java.time.Instant

class ProductResponse(
    val id: Long,
    val status: ProductStatus,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val stock: Long,
    val createdAt: Instant,
) {
    companion object {
        fun from(product: ProductView): ProductResponse =
            ProductResponse(
                id = product.id.value,
                status = product.status,
                name = product.name,
                description = product.description,
                price = product.price,
                stock = product.stock,
                createdAt = product.createdAt
            )
    }
}
