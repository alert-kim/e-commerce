package kr.hhplus.be.server.interfaces.coupon

import kr.hhplus.be.server.interfaces.ApiTest
import kr.hhplus.be.server.interfaces.ErrorCode
import kr.hhplus.be.server.mock.IdMock
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import java.time.Instant

class GetUserCouponsApiTest : ApiTest() {

    @Test
    fun `내 쿠폰 목록 조회 - 200`() {
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
    fun `내 쿠폰 목록 조회 - 200 - 빈 쿠폰 목록`() {
        val user = savedUser()

        mockMvc.get("/users/${user.id().value}/coupons")
            .andExpect {
                status { isOk() }
                jsonPath("$.coupons", hasSize<Any>(0))
            }
    }

    @Test
    fun `내 쿠폰 목록 조회 - 404 - 찾을 수 없는 유저`() {
        val userId = IdMock.value()

        mockMvc.get("/users/${userId}/coupons")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.errorCode") { value(ErrorCode.NOT_FOUND_USER.name) }
            }
    }
}
