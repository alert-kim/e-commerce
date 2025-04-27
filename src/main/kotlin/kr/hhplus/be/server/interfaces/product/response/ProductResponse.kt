package kr.hhplus.be.server.interfaces.product.response

import kr.hhplus.be.server.application.product.result.ProductsResult
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
    val stock: Int,
    val createdAt: Instant,
) {
    companion object {
        fun from(productWithStock: ProductsResult.ProductWithStock): ProductResponse =
            ProductResponse(
                id = productWithStock.product.id.value,
                status = productWithStock.product.status,
                name = productWithStock.product.name,
                description = productWithStock.product.description,
                price = productWithStock.product.price.value,
                stock = productWithStock.stockQuantity,
                createdAt = productWithStock.product.createdAt
            )
    }
}
