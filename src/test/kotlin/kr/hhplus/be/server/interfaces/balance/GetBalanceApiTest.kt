package kr.hhplus.be.server.interfaces.balance

import kr.hhplus.be.server.common.cache.CacheNames
import kr.hhplus.be.server.interfaces.ApiTest
import kr.hhplus.be.server.interfaces.ErrorCode
import kr.hhplus.be.server.testutil.mock.IdMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.test.web.servlet.get
import java.math.BigDecimal

class GetBalanceApiTest @Autowired constructor(
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

        mockMvc.get("/balances") {
            param("userId", "${user.id().value}")
        }.andExpect {
            status { isOk() }
            jsonPath("$.userId") { value(balance.userId.value) }
            jsonPath("$.balance") { value(balance.amount.value.toDouble()) }
        }
    }

    @Test
    @DisplayName("잔고가 없는 경우 0 값 반환")
    fun noBalance() {
        val user = savedUser()

        mockMvc.get("/balances") {
            param("userId", "${user.id().value}")
        }.andExpect {
            status { isOk() }
            jsonPath("$.userId") { value(user.id().value) }
            jsonPath("$.balance") { value(BigDecimal.ZERO) }
        }
    }

    @Test
    @DisplayName("존재하지 않는 유저 시 404 응답")
    fun userNotFound() {
        val userId = IdMock.value()

        mockMvc.get("/balances") {
            param("userId", "${userId}")
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.errorCode") { value(ErrorCode.NOT_FOUND_USER.name) }
        }
    }
}
