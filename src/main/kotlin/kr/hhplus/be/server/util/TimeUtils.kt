package kr.hhplus.be.server.util

import java.time.ZoneId


object TimeZone {
    val KSTId: ZoneId = ZoneId.of("Asia/Seoul")
    val UTCId: ZoneId = ZoneId.of("UTC")
}
