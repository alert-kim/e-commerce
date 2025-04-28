package kr.hhplus.be.server.domain.order.exception

import kr.hhplus.be.server.domain.DomainException
import kr.hhplus.be.server.domain.coupon.CouponId
import kr.hhplus.be.server.domain.order.OrderId
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
    productId: ProductId? = null,
    detail: String,
) : OrderException() {
    override val message: String = "상품(${productId?.value})에 대한 주문 가격이 유효하지 않습니다. $detail"
}

class InvalidOrderCouponException(
    orderId: OrderId,
    detail: String,
) : OrderException() {
    override val message: String = "주문(${orderId.value})에 대한 쿠폰이 유효하지 않습니다. $detail"
}

class InvalidOrderStatusException(
    id: OrderId,
    status: OrderStatus,
    expect: OrderStatus,
) : OrderException() {
    override val message: String = "주문(${id.value})의 상태($status)가 유효하지 않습니다. expect: $expect"
}

class InvalidOrderProductQuantityException(
    detail: String
) : OrderException() {
    override val message: String = "주문 상품의 수량이 유효하지 않습니다. $detail"
}

class AlreadyCouponAppliedException(
    id: OrderId,
    couponId: CouponId,
    newCouponId: CouponId,
) : OrderException() {
    override val message: String = "주문(${id.value})에 쿠폰(${couponId.value})이 이미 적용되어 있습니다 (적용 시도 쿠폰 : ${newCouponId.value})"
}

// order-product
class RequiredOrderProductIdException : OrderException() {
    override val message: String = "주문 상품 ID가 필요합니다."
}

// event
class RequiredOrderEventIdException : OrderException() {
    override val message: String = "주문 이벤트 ID가 필요합니다."
}
