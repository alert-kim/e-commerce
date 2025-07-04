package kr.hhplus.be.server.interfaces.product.api.response

import kr.hhplus.be.server.application.product.result.GetProductsFacadeResult
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
        fun from(productWithStock: GetProductsFacadeResult.ProductWithStock): ProductResponse =
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
