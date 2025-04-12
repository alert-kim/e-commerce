package kr.hhplus.be.server.domain.common

import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.Sort

class PageUtilsTest {

    @Test
    fun `createPageRequest - 유효한 페이지 요청인 경우 해당하는 PageRequest를 반환`() {
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
    fun `createPageRequest - page 값이 음수인 경우 InvalidPageRequestArgumentException 발생`() {
        val page = Arb.int(-10, -1).next()
        val pageSize = 10
        val sort = Sort.by("name").ascending()

        assertThrows<InvalidPageRequestArgumentException> {
            createPageRequest(page, pageSize, sort)
        }
    }

    @Test
    fun `createPageRequest - pageSize 값이 0인 경우 InvalidPageRequestArgumentException 발생`() {
        val page = 0
        val pageSize = 0
        val sort = Sort.by("name").ascending()

        assertThrows<InvalidPageRequestArgumentException> {
            createPageRequest(page, pageSize, sort)
        }
    }

    @Test
    fun `createPageRequest - pageSize 값이 음수인 경우 InvalidPageRequestArgumentException 발생`() {
        val page = Arb.int(-10, -1).next()
        val pageSize = 0
        val sort = Sort.by("name").ascending()

        assertThrows<InvalidPageRequestArgumentException> {
            createPageRequest(page, pageSize, sort)
        }
    }

}
