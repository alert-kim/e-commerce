package kr.hhplus.be.server.domain.user

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.user.exception.NotFoundUserException
import kr.hhplus.be.server.testutil.mock.IdMock
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

class UserServiceTest {

    private val cacheReader: UserCacheReader = mockk(relaxed = true)
    private val service: UserService = UserService(cacheReader)

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @Nested
    @DisplayName("유저 조회")
    inner class GetUser {

        @Test
        @DisplayName("유저 ID로 유저를 조회해 반환한다")
        fun returnUser() {
            val userId = UserMock.id()
            val user = UserMock.user(id = userId)
            every { cacheReader.getOrNull(userId.value) } returns user

            val result = service.get(userId.value)

            assertAll(
                { assertThat(result.id).isEqualTo(userId) },
                { assertThat(result.name).isEqualTo(user.name) },
                { assertThat(result.createdAt).isEqualTo(user.createdAt) },
            )
            verify { cacheReader.getOrNull(userId.value) }
        }

        @Test
        @DisplayName("해당 유저가 없는 경우 NotFoundUserException 발생")
        fun throwException() {
            val userId = IdMock.value()
            every { cacheReader.getOrNull(userId) } returns null

            assertThrows<NotFoundUserException> {
                service.get(userId)
            }

            verify { cacheReader.getOrNull(userId) }
        }
    }
}
