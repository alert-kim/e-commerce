package kr.hhplus.be.server.model.order

enum class OrderStatus {
    READY, STOCK_ALLOCATED, STOCK_ALLOCATION_FAILED, PAID, PAYMENT_FAILED, DELIVERED, DELIVERY_FAILED, COMPLETED
}
