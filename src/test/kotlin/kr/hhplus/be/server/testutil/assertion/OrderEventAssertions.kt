package kr.hhplus.be.server.testutil.assertion

import kr.hhplus.be.server.domain.order.event.OrderJpaEvent
import org.assertj.core.api.AbstractAssert

class OrderEventAssert(actual: OrderJpaEvent?) : AbstractAssert<OrderEventAssert, OrderJpaEvent>(actual, OrderEventAssert::class.java) {

    fun isEqualTo(expected: OrderJpaEvent): OrderEventAssert {
        when {
            actual == null -> failWithMessage("OrderEvent is null")
            actual.id().value != expected.id().value -> failWithMessage("OrderEvent id is not equal to expected")
            actual.orderId != expected.orderId -> failWithMessage("OrderEvent orderId is not equal to expected")
            actual.type != expected.type -> failWithMessage("OrderEvent type is not equal to expected")
            actual.createdAt != expected.createdAt -> failWithMessage("OrderEvent createdAt is not equal to expected")
            actual.snapshot != expected.snapshot -> failWithMessage("OrderEvent snapshot is not equal to expected")
        }
        return this
    }

    companion object {
        fun assertOrderEvent(actual: OrderJpaEvent?): OrderEventAssert {
            return OrderEventAssert(actual)
        }
    }
}
