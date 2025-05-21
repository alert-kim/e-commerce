package kr.hhplus.be.server.domain.order

interface OrderSender {
    fun send(snapshot: OrderSnapshot)
}
