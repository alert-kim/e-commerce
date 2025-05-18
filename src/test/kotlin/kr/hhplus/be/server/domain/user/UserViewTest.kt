package kr.hhplus.be.server.domain.user

import kr.hhplus.be.server.domain.user.exception.RequiredUserIdException
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows

class UserViewTest {

    @Nested
    @DisplayName("유저 변환")
    inner class From {

        @Test
        @DisplayName("유저 정보를 올바르게 변환한다")
        fun convertUser() {
            val user = UserMock.user()

            val result = UserView.from(user)

            assertAll(
                { assertThat(result.id).isEqualTo(user.id()) },
                { assertThat(result.name).isEqualTo(user.name) },
                { assertThat(result.createdAt).isEqualTo(user.createdAt) },
            )
        }

        @Test
        @DisplayName("유저 ID가 null이면 RequiredUserIdException 발생")
        fun throwException() {
            val user = UserMock.user(id = null)

            assertThrows<RequiredUserIdException> {
                UserView.from(user)
            }
        }
    }
}
