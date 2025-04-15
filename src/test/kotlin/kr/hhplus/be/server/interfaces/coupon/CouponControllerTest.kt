package kr.hhplus.be.server.interfaces.coupon

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kr.hhplus.be.server.application.coupon.CouponFacade
import kr.hhplus.be.server.domain.coupon.CouponSourceQueryModel
import kr.hhplus.be.server.mock.CouponMock
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(CouponController::class)
@ExtendWith(MockKExtension::class)
class CouponControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var couponFacade: CouponFacade

    @TestConfiguration
    class Config {
        @Bean
        fun couponFacade(): CouponFacade = mockk(relaxed = true)
    }

    @BeforeEach
    fun setUp() {
        clearMocks(couponFacade)
    }

    @Test
    fun `쿠폰 목록 조회 - 200`() {
        val coupons = List(3) {
            CouponMock.sourceQueryModel(
                name = "상품${it + 1}",
            )
        }
        every { couponFacade.getAllIssuable() } returns coupons

        mockMvc.get("/coupons")
            .andExpect {
                status { isOk() }
                jsonPath("$.coupons") { hasSize<Any>(coupons.size) }
                coupons.forEachIndexed { index, coupon ->
                    jsonPath("$.coupons[$index].id") { value(coupon.id.value) }
                    jsonPath("$.coupons[$index].name") { value(coupon.name) }
                    jsonPath("$.coupons[$index].quantity") { value(coupon.quantity) }
                    jsonPath("$.coupons[$index].discountAmount") { value(coupon.discountAmount.toString()) }
                    jsonPath("$.coupons[$index].createdAt") { value(coupon.createdAt.toString()) }
                    jsonPath("$.coupons[$index].updatedAt") { value(coupon.updatedAt.toString()) }
                }
            }
    }

    @Test
    fun `쿠폰 목록 조회 - 200 - 빈 쿠폰 목록`() {
        val coupons = emptyList<CouponSourceQueryModel>()
        every { couponFacade.getAllIssuable() } returns coupons

        mockMvc.get("/coupons")
            .andExpect {
                status { isOk() }
                jsonPath("$.coupons") { hasSize<Any>(0) }
            }
    }
}
