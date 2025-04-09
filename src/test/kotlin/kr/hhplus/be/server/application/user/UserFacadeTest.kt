package kr.hhplus.be.server.application.user

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kr.hhplus.be.server.domain.user.UserService
import kr.hhplus.be.server.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class UserFacadeTest {
    @InjectMockKs
    private lateinit var facade: UserFacade

    @MockK(relaxed = true)
    private lateinit var service: UserService

    @Test
    fun `유저 Id로 유저를 조회해 반환한다`() {
        val userId = UserMock.id()
        val user = UserMock.user(id = userId)
        every { service.get(userId.value) } returns user

        val result = facade.get(userId.value)

        assertAll(
            { assertThat(result.id).isEqualTo(userId) },
            { assertThat(result.name).isEqualTo(user.name) },
            { assertThat(result.createdAt).isEqualTo(user.createdAt) },
        )
        verify { service.get(userId.value) }
    }
}
