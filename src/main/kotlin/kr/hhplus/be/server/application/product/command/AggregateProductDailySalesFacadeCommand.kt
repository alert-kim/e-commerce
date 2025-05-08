package kr.hhplus.be.server.application.product.command

import java.time.LocalDate

data class AggregateProductDailySalesFacadeCommand(
    val date: LocalDate,
)
