package kr.hhplus.be.server.testutil.assertion

import kr.hhplus.be.server.domain.order.event.OrderEventConsumerOffset
import org.assertj.core.api.AbstractAssert

class OrderEventConsumerOffsetAsserts(actual: OrderEventConsumerOffset?) :
    AbstractAssert<OrderEventConsumerOffsetAsserts, OrderEventConsumerOffset>(actual, OrderEventConsumerOffsetAsserts::class.java) {

    fun isEqualTo(expected: OrderEventConsumerOffset): OrderEventConsumerOffsetAsserts {
        when {
            actual == null -> failWithMessage("OrderEventConsumerOffset is null")
            actual.id != expected.id -> failWithMessage("OrderEventConsumerOffset id is not equal to expected")
            actual.eventId != expected.eventId -> failWithMessage("OrderEventConsumerOffset eventId is not equal to expected")
            actual.createdAt != expected.createdAt -> failWithMessage("OrderEventConsumerOffset createdAt is not equal to expected")
            actual.updatedAt != expected.updatedAt -> failWithMessage("OrderEventConsumerOffset updatedAt is not equal to expected")
        }
        return this
    }
    
    companion object {
        fun assertOrderEventConsumerOffset(actual: OrderEventConsumerOffset?): OrderEventConsumerOffsetAsserts {
            return OrderEventConsumerOffsetAsserts(actual)
        }
    }
}
