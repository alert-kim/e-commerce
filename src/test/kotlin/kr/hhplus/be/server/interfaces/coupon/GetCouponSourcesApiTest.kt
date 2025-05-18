package kr.hhplus.be.server.interfaces.coupon

import kr.hhplus.be.server.domain.coupon.CouponSourceStatus
import kr.hhplus.be.server.interfaces.ApiTest
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.springframework.test.web.servlet.get

@Isolated
class GetCouponSourcesApiTest : ApiTest() {

    @BeforeEach
    fun setUp() {
       clearCouponSource()
    }

    @Test
    @DisplayName("발급 가능 쿠폰 목록 조회 - 200")
    fun getIssuableCouponSources() {
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
    @DisplayName("발급 가능 쿠폰 목록 조회 - 200 - 빈 쿠폰 목록")
    fun getEmptyCouponSources() {
        mockMvc.get("/couponSources")
            .andExpect {
                status { isOk() }
                jsonPath("$.coupons", hasSize<Any>(0))
            }
    }
}
