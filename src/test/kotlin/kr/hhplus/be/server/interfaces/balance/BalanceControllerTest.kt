package kr.hhplus.be.server.interfaces.balance

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.balance.BalanceFacade
import kr.hhplus.be.server.domain.user.exception.NotFoundUserException
import kr.hhplus.be.server.interfaces.ErrorCode
import kr.hhplus.be.server.application.user.UserFacade
import kr.hhplus.be.server.mock.BalanceMock
import kr.hhplus.be.server.mock.UserMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.math.BigDecimal

@WebMvcTest(BalanceController::class)
@ExtendWith(MockKExtension::class)
class BalanceControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var balanceFacade: BalanceFacade

    @Autowired
    private lateinit var userFacade: UserFacade

    @TestConfiguration
    class Config {
        @Bean
        fun balanceFacade(): BalanceFacade = mockk()

        @Bean
        fun userFacade(): UserFacade = mockk()
    }

    @BeforeEach
    fun setUp() {
        clearMocks(userFacade, balanceFacade)
    }

    @Test
    fun `잔고 조회 - 200 OK`() {
        val user = UserMock.queryModel()
        val balance = BalanceMock.queryModel(userId = user.id)
        every { userFacade.get(user.id.value) } returns user
        every { balanceFacade.getOrNullByUerId(user.id) } returns balance

        mockMvc.get("/balances") {
            param("userId", "${user.id.value}")
        }.andExpect {
            status { isOk() }
            jsonPath("$.userId") { value(balance.userId.value) }
            jsonPath("$.amount") { value(balance.amount.toPlainString()) }
            jsonPath("$.createdAt") { value(balance.createdAt.toString()) }
            jsonPath("$.updatedAt") { value(balance.updatedAt.toString()) }
        }

        verify { userFacade.get(user.id.value) }
        verify { balanceFacade.getOrNullByUerId(user.id) }
    }

    @Test
    fun `잔고 조회 - 유저를 찾을 수 없는 경우 404 Not Found`() {
        val userId = UserMock.id()
        every { userFacade.get(userId.value) } throws NotFoundUserException()

        mockMvc.get("/balances") {
            param("userId", "${userId.value}")
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.errorCode") { value(ErrorCode.NOT_FOUND_USER.name) }
        }

        verify { userFacade.get(userId.value) }
    }

    @Test
    fun `잔고조회 - 유저의 잔고가 없는 경우 잔고를 0으로 반환한다`() {
        val user = UserMock.queryModel()
        every { userFacade.get(user.id.value) } returns user
        every { balanceFacade.getOrNullByUerId(user.id) } returns null

        mockMvc.get("/balances") {
            param("userId", "${user.id.value}")
        }.andExpect {
            status { isOk() }
            jsonPath("$.userId") { value(user.id.value) }
            jsonPath("$.amount") { value(BigDecimal.ZERO.toPlainString()) }
        }

        verify { userFacade.get(user.id.value) }
        verify { balanceFacade.getOrNullByUerId(user.id) }
    }
}
