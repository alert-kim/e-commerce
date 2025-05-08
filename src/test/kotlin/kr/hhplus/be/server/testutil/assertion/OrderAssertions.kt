package kr.hhplus.be.server.testutil.assertion

import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderProduct
import org.assertj.core.api.AbstractAssert

class OrderAssert(actual: Order?) : AbstractAssert<OrderAssert, Order>(actual, OrderAssert::class.java) {

    fun isEqualTo(expected: Order): OrderAssert {
        when {
            actual == null -> failWithMessage("Order is null")
            actual.id() != expected.id() -> failWithMessage("Order id is not equal to expected")
            actual.userId != expected.userId -> failWithMessage("Order userId is not equal to expected")
            actual.status != expected.status -> failWithMessage("Order status is not equal to expected")
            actual.originalAmount.compareTo(expected.originalAmount) != 0 -> failWithMessage("Order originalAmount is not equal to expected")
            actual.discountAmount.compareTo(expected.discountAmount) != 0 -> failWithMessage("Order discountAmount is not equal to expected")
            actual.totalAmount.compareTo(expected.totalAmount) != 0 -> failWithMessage("Order totalAmount is not equal to expected")
            actual.couponId != expected.couponId -> failWithMessage("Order couponId is not equal to expected")
            actual.createdAt != expected.createdAt -> failWithMessage("Order createdAt is not equal to expected")
            actual.updatedAt != expected.updatedAt -> failWithMessage("Order updatedAt is not equal to expected")
        }
        actual.products.areEqualTo(expected.products)
        return this
    }

    private fun List<OrderProduct>.areEqualTo(expected: List<OrderProduct>) {
        if (this.size != expected.size) {
            failWithMessage("OrderProduct list size is not equal to expected")
        }
        this.forEachIndexed { index, actualProduct ->
            OrderProductAssert.Companion.assertOrderProduct(actualProduct).isEqualTo(expected[index])
        }
    }

    companion object {
        fun assertOrder(actual: Order?): OrderAssert {
            return OrderAssert(actual)
        }
    }
}

class OrderProductAssert(actual: OrderProduct?) :
    AbstractAssert<OrderProductAssert, OrderProduct>(actual, OrderProductAssert::class.java) {

    fun isEqualTo(expected: OrderProduct): OrderProductAssert {
        when {
            actual == null -> failWithMessage("OrderProduct is null")
            actual.id() != expected.id() -> failWithMessage("OrderProduct id is not equal to expected")
            actual.orderId() != expected.orderId() -> failWithMessage("OrderProduct orderId is not equal to expected")
            actual.productId != expected.productId -> failWithMessage("OrderProduct productId is not equal to expected")
            actual.quantity != expected.quantity -> failWithMessage("OrderProduct quantity is not equal to expected")
            actual.unitPrice.compareTo(expected.unitPrice) != 0 -> failWithMessage("OrderProduct unitPrice is not equal to expected")
            actual.totalPrice.compareTo(expected.totalPrice) != 0 -> failWithMessage("OrderProduct totalPrice is not equal to expected")
            actual.createdAt != expected.createdAt -> failWithMessage("OrderProduct createdAt is not equal to expected")
        }
        return this
    }

    companion object {
        fun assertOrderProduct(actual: OrderProduct?): OrderProductAssert {
            return OrderProductAssert(actual)
        }
    }
}
