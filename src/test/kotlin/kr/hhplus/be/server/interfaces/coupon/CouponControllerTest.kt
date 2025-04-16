package kr.hhplus.be.server.interfaces.coupon

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kr.hhplus.be.server.application.coupon.CouponFacade
import kr.hhplus.be.server.application.coupon.command.IssueCouponFacadeCommand
import kr.hhplus.be.server.domain.coupon.CouponSourceQueryModel
import kr.hhplus.be.server.domain.coupon.exception.NotFoundCouponSourceException
import kr.hhplus.be.server.domain.coupon.exception.OutOfStockCouponSourceException
import kr.hhplus.be.server.domain.user.exception.NotFoundUserException
import kr.hhplus.be.server.interfaces.ErrorCode
import kr.hhplus.be.server.interfaces.coupon.request.IssueCouponRequest
import kr.hhplus.be.server.mock.CouponMock
import kr.hhplus.be.server.mock.UserMock
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(CouponController::class)
@ExtendWith(MockKExtension::class)
class CouponControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

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
    fun `발급 가능 쿠폰 목록 조회 - 200`() {
        val coupons = List(3) {
            CouponMock.sourceQueryModel(
                name = "상품${it + 1}",
            )
        }
        every { couponFacade.getAllSourcesIssuable() } returns coupons

        mockMvc.get("/couponSources")
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
    fun `발급 가능 쿠폰 목록 조회 - 200 - 빈 쿠폰 목록`() {
        val coupons = emptyList<CouponSourceQueryModel>()
        every { couponFacade.getAllSourcesIssuable() } returns coupons

        mockMvc.get("/couponSources")
            .andExpect {
                status { isOk() }
                jsonPath("$.coupons") { hasSize<Any>(0) }
            }
    }

    @Test
    fun `내 쿠폰 목록 조회 - 200`() {
        val userId = UserMock.id()
        val coupons = List(3) {
            CouponMock.couponQueryModel(userId = userId)
        }
        every { couponFacade.getCoupons(userId.value) } returns coupons

        mockMvc.get("/users/${userId.value}/coupons")
            .andExpect {
                status { isOk() }
                jsonPath("$.coupons") { hasSize<Any>(coupons.size) }
                coupons.forEachIndexed { index, coupon ->
                    jsonPath("$.coupons[$index].id") { value(coupon.id.value) }
                    jsonPath("$.coupons[$index].userId") { value(coupon.userId.value) }
                    jsonPath("$.coupons[$index].name") { value(coupon.name) }
                    jsonPath("$.coupons[$index].discountAmount") { value(coupon.discountAmount.toString()) }
                    if (coupon.usedAt == null) {
                        jsonPath("$.coupons[$index].usedAt") { doesNotExist() }
                    } else {
                        jsonPath("$.coupons[$index].usedAt") { value(coupon.usedAt.toString()) }
                    }
                    jsonPath("$.coupons[$index].createdAt") { value(coupon.createdAt.toString()) }
                    jsonPath("$.coupons[$index].updatedAt") { value(coupon.updatedAt.toString()) }
                }
            }
    }

    @Test
    fun `내 쿠폰 목록 조회 - 200 - 빈 쿠폰 목록`() {
        val userId = UserMock.id()
        every { couponFacade.getCoupons(userId.value) } returns emptyList()

        mockMvc.get("/users/${userId.value}/coupons")
            .andExpect {
                status { isOk() }
                jsonPath("$.coupons") { hasSize<Any>(0) }
            }
    }

    @Test
    fun `내 쿠폰 목록 조회 - 404 - 유저 없음`() {
        val userId = UserMock.id()
        every { couponFacade.getCoupons(userId.value) } returns emptyList()

        mockMvc.get("/users/${userId.value}/coupons")
            .andExpect {
                status { isOk() }
                jsonPath("$.coupons") { hasSize<Any>(0) }
            }
    }

    @Test
    fun `내 쿠폰 목록 조회 - 404 - 찾을 수 없는 유저`() {
        val userId = UserMock.id()
        every { couponFacade.getCoupons(userId.value) } throws NotFoundUserException()

        mockMvc.get("/users/${userId.value}/coupons")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.errorCode") { value(ErrorCode.NOT_FOUND_USER.name) }
            }
    }

    @Test
    fun `쿠폰 발급 - 200`() {
        val couponSourceId = CouponMock.sourceId()
        val userId = UserMock.id()
        val coupon = CouponMock.couponQueryModel()
        val request = IssueCouponRequest(
            couponSourceId = couponSourceId.value,
            userId = userId.value,
        )
        every {
            couponFacade.issueCoupon(
                IssueCouponFacadeCommand(
                    couponSourceId = couponSourceId.value,
                    userId = userId.value
                ),
            )
        } returns coupon

        mockMvc.post("/coupons") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(coupon.id.value) }
            jsonPath("$.userId") { value(coupon.userId.value) }
            jsonPath("$.name") { value(coupon.name) }
            jsonPath("$.discountAmount") { value(coupon.discountAmount.toString()) }
            if (coupon.usedAt == null) {
                jsonPath("$.usedAt") { doesNotExist() }
            } else {
                jsonPath("$.usedAt") { value(coupon.usedAt.toString()) }
            }
            jsonPath("$.createdAt") { value(coupon.createdAt.toString()) }
            jsonPath("$.updatedAt") { value(coupon.updatedAt.toString()) }
        }
    }

    @Test
    fun `쿠폰 발급 - 400 - 쿠폰 재고 부족`() {
        val couponSourceId = CouponMock.sourceId()
        val userId = UserMock.id()
        val request = IssueCouponRequest(
            couponSourceId = couponSourceId.value,
            userId = userId.value,
        )
        every {
            couponFacade.issueCoupon(
                IssueCouponFacadeCommand(
                    couponSourceId = couponSourceId.value,
                    userId = userId.value,
                )
            )
        } throws OutOfStockCouponSourceException(couponSourceId, 1, 0)

        mockMvc.post("/coupons") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value(ErrorCode.OUT_OF_STOCK_COUPON_SOURCE.name) }
        }
    }

    @Test
    fun `쿠폰 발급 - 404 - 찾을 수 없는 유저`() {
        val couponSourceId = CouponMock.sourceId()
        val userId = UserMock.id()
        val request = IssueCouponRequest(
            couponSourceId = couponSourceId.value,
            userId = userId.value,
        )
        every {
            couponFacade.issueCoupon(
                IssueCouponFacadeCommand(
                    couponSourceId = couponSourceId.value,
                    userId = userId.value,
                )
            )
        } throws NotFoundUserException("")

        mockMvc.post("/coupons") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.errorCode") { value(ErrorCode.NOT_FOUND_USER.name) }
        }
    }

    @Test
    fun `쿠폰 발급 - 404 - 찾을 수 없는 쿠폰 소스`() {
        val couponSourceId = CouponMock.sourceId()
        val userId = UserMock.id()
        val request = IssueCouponRequest(
            couponSourceId = couponSourceId.value,
            userId = userId.value,
        )
        every {
            couponFacade.issueCoupon(
                IssueCouponFacadeCommand(
                    couponSourceId = couponSourceId.value,
                    userId = userId.value,
                )
            )
        } throws NotFoundCouponSourceException("")

        mockMvc.post("/coupons") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.errorCode") { value(ErrorCode.NOT_FOUND_COUPON_SOURCE.name) }
        }
    }
}
