package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.order.command.*
import kr.hhplus.be.server.domain.order.result.CreateOrderResult
import org.springframework.stereotype.Service

@Service
class OrderService {

    fun createOrder(
        command: CreateOrderCommand,
    ): CreateOrderResult {
        TODO()
    }

    fun placeStock(
        command: PlaceStockCommand,
    )  {
        TODO()
    }

    fun applyCoupon(
        command: ApplyCouponCommand,
    ) {
        TODO()
    }

    fun pay(
        command: PayOrderCommand,
    ) {
        TODO("Not yet implemented")
    }

    fun get(
        id: Long,
    ): Order {
        TODO(
            "Not yet implemented"
        )
    }
}
