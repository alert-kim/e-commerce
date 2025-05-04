package kr.hhplus.be.server.domain.product.stat.command // 커맨드 관련 패키지 (예시)

import java.time.LocalDate

data class CreateProductDailySaleStatsCommand(
    val date: LocalDate,
)
