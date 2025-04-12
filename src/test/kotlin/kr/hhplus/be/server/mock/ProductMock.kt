package kr.hhplus.be.server.mock

import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.product.ProductQueryModel
import kr.hhplus.be.server.domain.product.ProductStatus
import java.math.BigDecimal
import java.time.Instant

object ProductMock {

    fun id() = ProductId(IdMock.value())

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
