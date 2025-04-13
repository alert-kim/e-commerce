package kr.hhplus.be.server.domain.order.exception

import kr.hhplus.be.server.domain.DomainException
import kr.hhplus.be.server.domain.product.ProductId

abstract class OrderException : DomainException()

class InvalidOrderPriceException(
    productId: ProductId,
    detail: String,
) : OrderException() {
    override val message: String = "상품(${productId.value})에 대한 주문 가격이 유효하지 않습니다. $detail"
}
