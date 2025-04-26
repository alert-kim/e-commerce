package kr.hhplus.be.server.domain.stock.exception

import kr.hhplus.be.server.domain.DomainException
import kr.hhplus.be.server.domain.order.exception.OrderException
import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.user.exception.UserException

abstract class StockException : DomainException()

class OutOfStockException(
    productId: ProductId,
    required: Int,
    remaining: Int,
) : StockException() {
    override val message: String = "상품(${productId.value})의 재고($remaining)가 부족합니다. 필요 수량: $required"
}

class NotFoundStockException(productId: ProductId) : StockException() {
    override val message = "해당 상품의 재고를 찾을 수 없습니다. productId=${productId.value}"
}

class InvalidStockQuantityToAllocateException(
    detail: String
) : OrderException() {
    override val message: String = "할당할 재고 수량이 유효하지 않습ㄴ디ㅏ. $detail"
}

class RequiredStockIdException() : StockException() {
    override val message: String = "재고 ID가 필요합니다"
}

