package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.domain.product.excpetion.RequiredProductIdException
import java.math.BigDecimal
import java.time.Instant

class Product(
    val id: ProductId? = null,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val createdAt: Instant,
    val stock: ProductStock,
    status: ProductStatus,
    updatedAt: Instant,
) {
    var status: ProductStatus = status
        private set

    var updatedAt: Instant = updatedAt
        private set

    fun requireId(): ProductId =
        id ?: throw RequiredProductIdException()
}
