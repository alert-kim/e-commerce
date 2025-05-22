package kr.hhplus.be.server.interfaces.coupon.api

import kr.hhplus.be.server.interfaces.ApiTest
import kr.hhplus.be.server.interfaces.ErrorCode
import kr.hhplus.be.server.testutil.mock.IdMock
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import java.time.Instant

class GetUserCouponsApiTest : ApiTest() {

    @Test
    @DisplayName("미사용 쿠폰 목록을 정상적으로 조회한다 - 200")
    fun success() {
        val user = savedUser()
        val coupons = List(3) {
            savedCoupon(userId = user.id(), usedAt = null)
        }
        savedCoupon(userId = user.id(), usedAt = Instant.now())

        mockMvc.get("/users/${user.id().value}/coupons")
            .andExpect {
                status { isOk() }
                jsonPath("$.coupons", hasSize<Any>(coupons.size))
                coupons.forEachIndexed { index, coupon ->
                    jsonPath("$.coupons[$index].id") { value(coupon.id().value) }
                    jsonPath("$.coupons[$index].userId") { value(coupon.userId.value) }
                    jsonPath("$.coupons[$index].name") { value(coupon.name) }
                    jsonPath("$.coupons[$index].discountAmount") { value(coupon.discountAmount.toDouble()) }
                    jsonPath("$.coupons[$index].usedAt") { doesNotExist() }
                    jsonPath("$.coupons[$index].createdAt") { value(coupon.createdAt.toString()) }
                    jsonPath("$.coupons[$index].updatedAt") { value(coupon.updatedAt.toString()) }
                }
            }
    }

    @Test
    @DisplayName("쿠폰이 없는 경우 빈 목록을 반환한다 - 200")
    fun empty() {
        val user = savedUser()

        mockMvc.get("/users/${user.id().value}/coupons")
            .andExpect {
                status { isOk() }
                jsonPath("$.coupons", hasSize<Any>(0))
            }
    }

    @Test
    @DisplayName("존재하지 않는 사용자는 404 오류를 반환한다")
    fun notFound() {
        val userId = IdMock.value()

        mockMvc.get("/users/${userId}/coupons")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.errorCode") { value(ErrorCode.NOT_FOUND_USER.name) }
            }
    }
}
