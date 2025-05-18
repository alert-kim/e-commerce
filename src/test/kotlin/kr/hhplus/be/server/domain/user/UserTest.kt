package kr.hhplus.be.server.domain.user

import kr.hhplus.be.server.domain.user.exception.RequiredUserIdException
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UserTest {

    @Nested
    @DisplayName("ID 확인")
    inner class RequireId {

        @Test
        @DisplayName("ID가 null이 아닌 경우 ID 반환")
        fun returnId() {
            val user = UserMock.user(id = UserMock.id())

            val result = user.id()

            assertThat(result).isEqualTo(user.id())
        }

        @Test
        @DisplayName("ID가 null이면 RequiredUserIdException 발생")
        fun throwException() {
            val user = UserMock.user(id = null)

            assertThrows<RequiredUserIdException> {
                user.id()
            }
        }
    }
}
