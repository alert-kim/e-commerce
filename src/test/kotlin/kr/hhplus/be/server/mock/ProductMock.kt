package kr.hhplus.be.server.mock

import kr.hhplus.be.server.domain.product.*
import java.math.BigDecimal
import java.time.Instant

object ProductMock {

    fun id() = ProductId(IdMock.value())

    fun stock(
        productId: ProductId = id(),
        quantity: Long = 10,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) = ProductStock(
        productId = productId,
        createdAt = createdAt,
        quantity = quantity,
        updatedAt = updatedAt,
    )

    fun product(
        id: ProductId? = id(),
        status: ProductStatus = ProductStatus.ON_SALE,
        name: String = "상품명",
        description: String = "상품 설명",
        price: BigDecimal = BigDecimal.valueOf(10_000),
        stock: ProductStock = stock(),
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ): Product = Product(
        id = id,
        status = status,
        name = name,
        description = description,
        price = price,
        stock = stock,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    fun queryModel(
        id: ProductId = id(),
        name: String = "상품명",
        description: String = "상품 설명",
        price: BigDecimal = BigDecimal.valueOf(10000),
        stock: Long = 10,
        status: ProductStatus = ProductStatus.ON_SALE,
        createdAt: Instant = Instant.now(),
    ) = ProductQueryModel(
        id = id,
        name = name,
        description = description,
        price = price,
        stock = stock,
        status = status,
        createdAt = createdAt,
    )
}
