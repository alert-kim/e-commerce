package kr.hhplus.be.server.interfaces.coupon.api

import kr.hhplus.be.server.domain.coupon.CouponSourceStatus
import kr.hhplus.be.server.interfaces.ApiTest
import kr.hhplus.be.server.interfaces.ErrorCode
import kr.hhplus.be.server.interfaces.coupon.api.request.IssueCouponRequest
import kr.hhplus.be.server.testutil.mock.IdMock
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post

class IssueCouponApiTest : ApiTest() {

    @Test
    @DisplayName("쿠폰 발급 - 200")
    fun issueCoupon() {
        val user = savedUser()
        val couponSource = savedCouponSource(
            status = CouponSourceStatus.ACTIVE
        )
        val request = IssueCouponRequest(
            couponSourceId = couponSource.id().value,
            userId = user.id().value
        )

        mockMvc.post("/coupons") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { exists() }
            jsonPath("$.userId") { value(user.id().value) }
            jsonPath("$.name") { value(couponSource.name) }
            jsonPath("$.discountAmount") { value(couponSource.discountAmount.toDouble()) }
            jsonPath("$.usedAt") { doesNotExist() }
            jsonPath("$.createdAt") { exists() }
            jsonPath("$.updatedAt") { exists() }
        }
    }

    @Test
    @DisplayName("쿠폰 발급 - 400 - 쿠폰 재고 부족")
    fun issueCouponOutOfStock() {
        val user = savedUser()
        val couponSource = savedCouponSource(
            quantity = 0,
            status = CouponSourceStatus.OUT_OF_STOCK
        )
        val request = IssueCouponRequest(
            couponSourceId = couponSource.id().value,
            userId = user.id().value
        )

        mockMvc.post("/coupons") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value(ErrorCode.OUT_OF_STOCK_COUPON_SOURCE.name) }
        }
    }

    @Test
    @DisplayName("쿠폰 발급 - 404 - 찾을 수 없는 유저")
    fun issueCouponUserNotFound() {
        val userId = IdMock.value()
        val couponSource = savedCouponSource()
        val request = IssueCouponRequest(
            couponSourceId = couponSource.id().value,
            userId = userId
        )

        mockMvc.post("/coupons") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.errorCode") { value(ErrorCode.NOT_FOUND_USER.name) }
        }
    }

    @Test
    @DisplayName("쿠폰 발급 - 404 - 찾을 수 없는 쿠폰 소스")
    fun issueCouponSourceNotFound() {
        val user = savedUser()
        val couponSourceId = IdMock.value()
        val request = IssueCouponRequest(
            couponSourceId = couponSourceId,
            userId = user.id().value
        )

        mockMvc.post("/coupons") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.errorCode") { value(ErrorCode.NOT_FOUND_COUPON_SOURCE.name) }
        }
    }
}
