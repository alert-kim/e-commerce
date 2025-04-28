package kr.hhplus.be.server.domain.user

import kr.hhplus.be.server.domain.user.exception.RequiredUserIdException
import kr.hhplus.be.server.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows

class UserViewTest {

    @Test
    fun `유저 정보를 올바르게 변환한다`() {
        val user = UserMock.user()

        val result = UserView.from(user)

        assertAll(
            { assertThat(result.id).isEqualTo(user.id()) },
            { assertThat(result.name).isEqualTo(user.name) },
            { assertThat(result.createdAt).isEqualTo(user.createdAt) },
        )
    }

    @Test
    fun `해당 유저의 아이디가 null이면 RequiredUserIdException가 발생한다`() {
        val user = UserMock.user(id = null)

        assertThrows<RequiredUserIdException> {
            UserView.from(user)
        }
    }
}
