package kr.hhplus.be.server.interfaces.balance

import kr.hhplus.be.server.interfaces.ApiTest
import kr.hhplus.be.server.interfaces.ErrorCode
import kr.hhplus.be.server.interfaces.balance.request.ChargeApiRequest
import kr.hhplus.be.server.mock.IdMock
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.math.BigDecimal
import java.math.RoundingMode

class ChargeBalanceApiTest : ApiTest() {

    @Test
    fun `잔고 충전 - 200`() {
        val user = savedUser()
        val balance = savedBalance(user.id())
        val request = ChargeApiRequest(userId = user.id().value, amount = BigDecimal.valueOf(1_000))
        val expectBalanceAmount = balance.amount.value.plus(request.amount).setScale(2, RoundingMode.HALF_UP)

        mockMvc.post("/balances/charge") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.userId") { value(balance.userId.value) }
            jsonPath("$.balance") { value(expectBalanceAmount) }
        }
    }

    @Test
    fun `잔고 충전 - 400 - 최소 잔고 미만`() {
        val user = savedUser()
        val request = ChargeApiRequest(user.id().value, BigDecimal.valueOf(-1))

        mockMvc.post("/balances/charge") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value(ErrorCode.BELOW_MIN_BALANCE.name) }
        }
    }

    @Test
    fun `잔고 충전 - 400 - 찾을 수 없는 유저`() {
        val userId = IdMock.value()
        val request = ChargeApiRequest(userId, BigDecimal.valueOf(1_000))

        mockMvc.post("/balances/charge") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.errorCode") { value(ErrorCode.NOT_FOUND_USER.name) }
        }
    }
}
