package kr.hhplus.be.server.domain.product.excpetion

import kr.hhplus.be.server.domain.DomainException
import kr.hhplus.be.server.domain.product.ProductPrice
import kr.hhplus.be.server.domain.product.ProductStatus
import java.math.BigDecimal

abstract class ProductException : DomainException()

class RequiredProductIdException : ProductException() {
    override val message = "상품 Id가 필요합니다"
}

class NotFoundProductException(
    detail: String,
) : ProductException() {
    override val message: String = "상품을 찾을 수 없습니다. $detail"
}

class ProductPriceMismatchException(
    id: Long,
    actual: BigDecimal,
    expect: BigDecimal,
): ProductException() {
    override val message: String = "상품($id) 가격이 일치하지 않습니다. 현재 가격: $actual, 요청 가격: $expect"
}

class NotOnSaleProductException(
    id: Long,
) : ProductException() {
    override val message: String = "상품($id)은 판매중이 아닙니다."
}



