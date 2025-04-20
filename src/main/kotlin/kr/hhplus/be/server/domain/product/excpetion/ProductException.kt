package kr.hhplus.be.server.domain.product.excpetion

import kr.hhplus.be.server.domain.DomainException

abstract class ProductException : DomainException()

class RequiredProductIdException : ProductException() {
    override val message = "상품 Id가 필요합니다"
}

class NotFoundProductException(
    detail: String,
) : ProductException() {
    override val message: String = "상품을 찾을 수 없습니다. $detail"
}

class OutOfStockProductException(
    productId: Long,
    required: Int,
    remaining: Long,
) : ProductException() {
    override val message: String = "상품($productId)의 재고($remaining)가 부족합니다. 필요 수량: $required"
}



