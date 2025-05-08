package kr.hhplus.be.server.domain.user

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kr.hhplus.be.server.domain.user.exception.NotFoundUserException
import kr.hhplus.be.server.domain.user.repository.UserRepository
import kr.hhplus.be.server.testutil.mock.IdMock
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class UserServiceTest {
    @InjectMockKs
    private lateinit var service: UserService

    @MockK(relaxed = true)
    private lateinit var repository: UserRepository

    @Test
    fun `유저 Id로 유저를 조회해 반환한다`() {
        val userId = UserMock.id()
        val user = UserMock.user(id = userId)
        every { repository.findById(userId.value) } returns user

        val result = service.get(userId.value)

        assertAll(
            { assertThat(result.id).isEqualTo(userId) },
            { assertThat(result.name).isEqualTo(user.name) },
            { assertThat(result.createdAt).isEqualTo(user.createdAt) },
        )
        verify { repository.findById(userId.value) }
    }

    @Test
    fun `해당 유저가 없는 경우, NotFoundUserException이 발생한다`() {
        val userId = IdMock.value()
        every { repository.findById(userId) } returns null

        assertThrows<NotFoundUserException> {
            service.get(userId)
        }

        verify { repository.findById(userId) }
    }
}
