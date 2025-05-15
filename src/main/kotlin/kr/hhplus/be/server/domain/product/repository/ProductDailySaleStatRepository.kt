package kr.hhplus.be.server.domain.product.repository

import java.time.LocalDate

interface ProductDailySaleStatRepository {
    fun aggregateDailyStatsByDate(date: LocalDate)
}
