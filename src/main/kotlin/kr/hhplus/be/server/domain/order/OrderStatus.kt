package kr.hhplus.be.server.domain.order

enum class OrderStatus {
    READY, STOCK_ALLOCATED, COMPLETED, FAILED, FAIL_HANDLED;
}
