package kr.hhplus.be.server.mock

import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next

object IdMock {
    fun value(): Long = Arb.long(0L..100L).next()
}
