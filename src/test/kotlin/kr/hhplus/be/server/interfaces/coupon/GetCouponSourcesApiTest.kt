package kr.hhplus.be.server.interfaces.coupon

import kr.hhplus.be.server.domain.coupon.CouponSourceStatus
import kr.hhplus.be.server.interfaces.ApiTest
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get

class GetCouponSourcesApiTest : ApiTest() {

    @Test
    fun `발급 가능 쿠폰 목록 조회 - 200`() {
        val issuableSources = List(3) {
            savedCouponSource(
                status = CouponSourceStatus.ACTIVE,
            )
        }
        savedCouponSource(
            status = CouponSourceStatus.OUT_OF_STOCK,
        )

        mockMvc.get("/couponSources")
            .andExpect {
                status { isOk() }
                jsonPath("$.coupons", hasSize<Any>(issuableSources.size))
                issuableSources.forEachIndexed { index, couponSource ->
                    jsonPath("$.coupons[$index].id") { value(couponSource.id().value) }
                    jsonPath("$.coupons[$index].name") { value(couponSource.name) }
                    jsonPath("$.coupons[$index].quantity") { value(couponSource.quantity) }
                    jsonPath("$.coupons[$index].discountAmount") { value(couponSource.discountAmount.toDouble()) }
                    jsonPath("$.coupons[$index].createdAt") { value(couponSource.createdAt.toString()) }
                    jsonPath("$.coupons[$index].updatedAt") { value(couponSource.updatedAt.toString()) }
                }
            }
    }

    @Test
    fun `발급 가능 쿠폰 목록 조회 - 200 - 빈 쿠폰 목록`() {
        mockMvc.get("/couponSources")
            .andExpect {
                status { isOk() }
                jsonPath("$.coupons", hasSize<Any>(0))
            }
    }
}
