package kr.hhplus.be.server.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime


object TimeZone {
    val KSTId: ZoneId = ZoneId.of("Asia/Seoul")
    val UTCId: ZoneId = ZoneId.of("UTC")
}

fun LocalDateNowKST(): LocalDate =
    LocalDate.now(TimeZone.KSTId)

fun InstantNowKST(): Instant =
    ZonedDateTime.now(TimeZone.KSTId).toInstant()
