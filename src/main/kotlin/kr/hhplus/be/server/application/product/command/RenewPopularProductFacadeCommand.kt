package kr.hhplus.be.server.application.product.command

import java.time.LocalDate

data class RenewPopularProductFacadeCommand(
    val date: LocalDate,
)
