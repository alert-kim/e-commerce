package kr.hhplus.be.server.domain.product.ranking.repository

import java.time.LocalDate

data class RenewProductSaleRankingCommand(
    val date: LocalDate,
)
