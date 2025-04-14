package kr.hhplus.be.server.domain.order.exception

import kr.hhplus.be.server.domain.DomainException
import kr.hhplus.be.server.domain.order.OrderStatus
import kr.hhplus.be.server.domain.product.ProductId

abstract class OrderException : DomainException()

class RequiredOrderIdException : OrderException() {
    override val message: String = "주문 ID가 필요합니다."
}

class NotFoundOrderException(
    detail: String,
) : OrderException() {
    override val message: String = "주문을 찾을 수 없습니다. : $detail"
}

class InvalidOrderPriceException(
    productId: ProductId,
    detail: String,
) : OrderException() {
    override val message: String = "상품(${productId.value})에 대한 주문 가격이 유효하지 않습니다. $detail"
}

class InvalidOrderStatusException(
    orderId: Long,
    status: OrderStatus,
    expect: OrderStatus,
) : OrderException() {
    override val message: String = "주문($orderId)의 상태($status)가 유효하지 않습니다. expect: $expect"
}
