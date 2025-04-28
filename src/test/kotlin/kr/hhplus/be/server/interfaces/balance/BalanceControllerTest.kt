package kr.hhplus.be.server.interfaces.balance

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.*
import io.mockk.junit5.MockKExtension
import kr.hhplus.be.server.application.balance.BalanceFacade
import kr.hhplus.be.server.application.balance.command.ChargeBalanceFacadeCommand
import kr.hhplus.be.server.application.user.UserFacade
import kr.hhplus.be.server.domain.balance.exception.BelowMinBalanceAmountException
import kr.hhplus.be.server.domain.balance.exception.ExceedMaxBalanceAmountException
import kr.hhplus.be.server.domain.user.exception.NotFoundUserException
import kr.hhplus.be.server.interfaces.ErrorCode
import kr.hhplus.be.server.interfaces.balance.request.ChargeApiRequest
import kr.hhplus.be.server.mock.BalanceMock
import kr.hhplus.be.server.mock.UserMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.math.BigDecimal

@WebMvcTest(BalanceController::class)
@ExtendWith(MockKExtension::class)
class BalanceControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

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
        val user = UserMock.view()
        val balance = BalanceMock.view(userId = user.id)
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

        verifyAll {
            userFacade.get(user.id.value)
            balanceFacade.getOrNullByUerId(user.id)
        }
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
    fun `잔고 조회 - 유저의 잔고가 없는 경우 잔고를 0으로 반환한다`() {
        val user = UserMock.view()
        every { userFacade.get(user.id.value) } returns user
        every { balanceFacade.getOrNullByUerId(user.id) } returns null

        mockMvc.get("/balances") {
            param("userId", "${user.id.value}")
        }.andExpect {
            status { isOk() }
            jsonPath("$.userId") { value(user.id.value) }
            jsonPath("$.amount") { value(BigDecimal.ZERO.toPlainString()) }
        }

        verifyAll {
            userFacade.get(user.id.value)
            balanceFacade.getOrNullByUerId(user.id)
        }
    }

    @Test
    fun `잔고 충전 - 200`() {
        val userId = UserMock.id()
        val balanceId = BalanceMock.id()
        val request = ChargeApiRequest(userId = userId.value, amount = BigDecimal.valueOf(1_000))
        val chargedBalance = BalanceMock.view(userId = userId, amount = BigDecimal.valueOf(2_000))
        every { balanceFacade.charge(ChargeBalanceFacadeCommand(userId = userId.value, amount = request.amount)) } returns chargedBalance

        mockMvc.post("/balances/charge") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.userId") { value(chargedBalance.userId.value) }
            jsonPath("$.amount") { value(chargedBalance.amount.toString()) }
            jsonPath("$.createdAt") { value(chargedBalance.createdAt.toString()) }
            jsonPath("$.updatedAt") { value(chargedBalance.updatedAt.toString()) }
        }

        verify {
            balanceFacade.charge(
               ChargeBalanceFacadeCommand(
                    userId = userId.value,
                    amount = request.amount,
                )
            )
        }
    }

    @Test
    fun `잔고 충전 - 400 - 최대 잔고를 초과`() {
        val userId = UserMock.id()
        val request = ChargeApiRequest(userId.value, BigDecimal.valueOf(1_000))
        every { balanceFacade.charge(any()) } throws ExceedMaxBalanceAmountException(request.amount)

        mockMvc.post("/balances/charge") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value(ErrorCode.EXCEED_MAX_BALANCE.name) }
        }

        verify { balanceFacade.charge(ChargeBalanceFacadeCommand(userId = request.userId, amount = request.amount)) }
    }

    @Test
    fun `잔고 충전 - 400 - 최소 잔고 미만`() {
        val userId = UserMock.id()
        val request = ChargeApiRequest(userId.value, BigDecimal.valueOf(1_000))
        every { balanceFacade.charge(any()) } throws BelowMinBalanceAmountException(request.amount)

        mockMvc.post("/balances/charge") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value(ErrorCode.BELOW_MIN_BALANCE.name) }
        }

        verify { balanceFacade.charge(ChargeBalanceFacadeCommand(userId = request.userId, amount = request.amount)) }
    }

    @Test
    fun `잔고 충전 - 400 - 찾을 수 없는 유저`() {
        val userId = 1L
        val request = ChargeApiRequest(userId, BigDecimal.valueOf(1_000))
        every { balanceFacade.charge(any()) } throws NotFoundUserException()

        mockMvc.post("/balances/charge") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.errorCode") { value(ErrorCode.NOT_FOUND_USER.name) }
        }

        verify { balanceFacade.charge(ChargeBalanceFacadeCommand(userId = request.userId, amount = request.amount)) }
    }
}
