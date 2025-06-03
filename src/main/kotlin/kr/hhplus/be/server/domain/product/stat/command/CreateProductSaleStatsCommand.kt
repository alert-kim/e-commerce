package kr.hhplus.be.server.domain.product.stat.command // 커맨드 관련 패키지 (예시)

import kr.hhplus.be.server.domain.order.OrderView

data class CreateProductSaleStatsCommand(
    val completedOrder: OrderView,
)
