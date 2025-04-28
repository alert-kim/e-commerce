package kr.hhplus.be.server.interfaces.balance

import kr.hhplus.be.server.interfaces.ApiTest
import kr.hhplus.be.server.interfaces.ErrorCode
import kr.hhplus.be.server.mock.IdMock
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import java.math.BigDecimal

class GetBalanceApiTest : ApiTest() {

    @Test
    fun `잔고 조회 - 200`() {
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
    fun `잔고 조회 - 200 - 유저의 잔고가 없는 경우 잔고를 0으로 반환`() {
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
    fun `잔고 조회 - 404 - 찾을 수 없는 유저`() {
        val userId = IdMock.value()

        mockMvc.get("/balances") {
            param("userId", "${userId}")
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.errorCode") { value(ErrorCode.NOT_FOUND_USER.name) }
        }
    }
}
