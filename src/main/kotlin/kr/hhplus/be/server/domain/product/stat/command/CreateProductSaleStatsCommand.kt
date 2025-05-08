    package kr.hhplus.be.server.domain.product.stat.command // 커맨드 관련 패키지 (예시)

    import kr.hhplus.be.server.domain.order.event.OrderCompletedEvent

    data class CreateProductSaleStatsCommand(
        val event: OrderCompletedEvent
    )
