package kr.hhplus.be.server.infra.product.ranking

import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal object ProductRankingRedisKeyGenerator {
    private const val KEY_PREFIX = "product:sale:rank:"
    private const val UNION_KEY_PREFIX = "product:sale:rank:union:"
    private val DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE

    fun dailyKey(date: LocalDate): String =
        KEY_PREFIX + date.format(DATE_FORMATTER)

    fun unionKey(start: LocalDate, end: LocalDate): String {
        require(start <= end) { "start date must be less than or equal to end date" }
        val start = start.format(DATE_FORMATTER)
        val end = end.format(DATE_FORMATTER)
        return "$UNION_KEY_PREFIX$start:$end"
    }
}
