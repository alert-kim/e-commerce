package kr.hhplus.be.server.domain.common

import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.Sort

class PageUtilsTest {

    @Nested
    @DisplayName("createPageRequest 메서드")
    inner class CreatePageRequest {

        @Test
        @DisplayName("유효한 요청이면 PageRequest를 반환한다")
        fun validRequest() {
            val page = 0
            val pageSize = 10
            val sort = Sort.by("name")

            val result = createPageRequest(page = page, pageSize = pageSize, sort = sort)

            assertAll(
                { assertThat(result.pageNumber).isEqualTo(page) },
                { assertThat(result.pageSize).isEqualTo(pageSize) },
                { assertThat(result.sort).isEqualTo(sort) },
            )
        }

        @Test
        @DisplayName("page 값이 음수면 예외를 발생시킨다")
        fun negativePage() {
            val page = Arb.int(-10, -1).next()
            val pageSize = 10
            val sort = Sort.by("name").ascending()

            assertThrows<InvalidPageRequestArgumentException> {
                createPageRequest(page, pageSize, sort)
            }
        }

        @Test
        @DisplayName("pageSize 값이 0이면 예외를 발생시킨다")
        fun zeroPageSize() {
            val page = 0
            val pageSize = 0
            val sort = Sort.by("name").ascending()

            assertThrows<InvalidPageRequestArgumentException> {
                createPageRequest(page, pageSize, sort)
            }
        }

        @Test
        @DisplayName("pageSize 값이 음수면 예외를 발생시킨다")
        fun negativePageSize() {
            val page = Arb.int(-10, -1).next()
            val pageSize = 0
            val sort = Sort.by("name").ascending()

            assertThrows<InvalidPageRequestArgumentException> {
                createPageRequest(page, pageSize, sort)
            }
        }
    }
}
