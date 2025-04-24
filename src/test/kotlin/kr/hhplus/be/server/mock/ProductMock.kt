package kr.hhplus.be.server.mock

import kr.hhplus.be.server.domain.product.*
import kr.hhplus.be.server.domain.product.result.ProductStockAllocated
import kr.hhplus.be.server.domain.product.result.PurchasableProduct
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

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

    fun stockAllocated(
        productId: ProductId = id(),
        quantity: Int = 10,
        unitPrice: BigDecimal = BigDecimal.valueOf(10_000),
    ): ProductStockAllocated = ProductStockAllocated(
        productId = productId,
        quantity = quantity,
        unitPrice = unitPrice,
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

    fun purchasableProduct(
        id: ProductId = id(),
        price: BigDecimal = BigDecimal.valueOf(10_000),
    ) = PurchasableProduct(
        id = id,
        price = ProductPrice(price),
    )

    fun view(
        id: ProductId = id(),
        name: String = "상품명",
        description: String = "상품 설명",
        price: BigDecimal = BigDecimal.valueOf(10000),
        stock: Long = 10,
        status: ProductStatus = ProductStatus.ON_SALE,
        createdAt: Instant = Instant.now(),
    ) = ProductView(
        id = id,
        name = name,
        description = description,
        price = ProductPrice(price),
        stock = stock,
        status = status,
        createdAt = createdAt,
    )

    fun dailySale(
        date: LocalDate = LocalDate.now(),
        productId: ProductId = id(),
        quantity: Int = 10,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) = ProductDailySale(
        date = date,
        productId = productId,
        createdAt = createdAt,
        quantity = quantity,
        updatedAt = updatedAt,
    )
}
