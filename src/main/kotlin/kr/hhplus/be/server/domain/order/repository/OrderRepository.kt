package kr.hhplus.be.server.domain.order.repository

import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderId

interface OrderRepository {
    fun save(order: Order): Order
    fun findById(orderId: Long): Order?
}
