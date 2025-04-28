package kr.hhplus.be.server.domain.user

import kr.hhplus.be.server.domain.user.exception.RequiredUserIdException
import kr.hhplus.be.server.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UserTest {

    @Test
    fun `requireId - id가 null이 아닌 경우 id 반환`() {
        val user = UserMock.user(id = UserMock.id())

        val result = user.requireId()

        assertThat(result).isEqualTo(user.id)
    }

    @Test
    fun `requireId - id가 null이면 RequiredUserIdException 발생`() {
        val user = UserMock.user(id = null)

        assertThrows(RequiredUserIdException::class.java) {
            user.requireId()
        }
    }
}
