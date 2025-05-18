package kr.hhplus.be.server.interfaces.balance

import kr.hhplus.be.server.common.cache.CacheNames
import kr.hhplus.be.server.interfaces.ApiTest
import kr.hhplus.be.server.interfaces.ErrorCode
import kr.hhplus.be.server.interfaces.balance.request.ChargeApiRequest
import kr.hhplus.be.server.testutil.mock.IdMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import java.math.BigDecimal
import java.math.RoundingMode

class ChargeBalanceApiTest @Autowired constructor(
    private val cacheManager: CacheManager
) : ApiTest() {

    @BeforeEach
    fun setup() {
        cacheManager.getCache(CacheNames.USER)?.clear()
    }

    @Test
    @DisplayName("정상 처리 시 200 응답")
    fun success() {
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
            jsonPath("$.balance") { value(expectBalanceAmount.toDouble()) }
        }
    }

    @Test
    @DisplayName("최소 잔고 미만 시 400 응답")
    fun belowMinimum() {
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
    @DisplayName("존재하지 않는 유저 시 404 응답")
    fun userNotFound() {
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
