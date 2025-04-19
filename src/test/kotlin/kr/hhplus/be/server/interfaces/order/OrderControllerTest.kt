package kr.hhplus.be.server.interfaces.order

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kr.hhplus.be.server.application.order.OrderFacade
import kr.hhplus.be.server.domain.balance.exception.InsufficientBalanceException
import kr.hhplus.be.server.domain.coupon.exception.AlreadyUsedCouponException
import kr.hhplus.be.server.domain.coupon.exception.ExpiredCouponException
import kr.hhplus.be.server.domain.coupon.exception.NotFoundCouponException
import kr.hhplus.be.server.domain.order.exception.InvalidOrderPriceException
import kr.hhplus.be.server.domain.product.excpetion.NotFoundProductException
import kr.hhplus.be.server.domain.product.excpetion.OutOfStockProductException
import kr.hhplus.be.server.domain.user.exception.NotFoundUserException
import kr.hhplus.be.server.interfaces.ErrorCode
import kr.hhplus.be.server.interfaces.order.reqeust.OrderRequest
import kr.hhplus.be.server.mock.CouponMock
import kr.hhplus.be.server.mock.OrderMock
import kr.hhplus.be.server.mock.ProductMock
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.math.BigDecimal

@WebMvcTest(OrderController::class)
@ExtendWith(MockKExtension::class)
class OrderControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var orderFacade: OrderFacade

    @TestConfiguration
    class Config {
        @Bean
        fun orderFacade(): OrderFacade = mockk()
    }

    @BeforeEach
    fun setUp() {
        clearMocks(orderFacade)
    }

    @Test
    fun `주문 - 200`() {
        val request = request()
        val order = OrderMock.view()
        every { orderFacade.order(any()) } returns order

        mockMvc.post("/orders") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(order.id.value) }
            jsonPath("$.userId") { value(order.userId.value) }
            jsonPath("$.status") { value(order.status.name) }
            if (order.couponId != null) {
                jsonPath("$.couponId") { value(order.couponId?.value) }
            } else {
                jsonPath("$.couponId") { value(null) }
            }
            jsonPath("$.originalAmount") { value(order.originalAmount.toPlainString()) }
            jsonPath("$.discountAmount") { value(order.discountAmount.toPlainString()) }
            jsonPath("$.totalAmount") { value(order.totalAmount.toPlainString()) }
            jsonPath("$.createdAt") { value(order.createdAt.toString()) }
            jsonPath("$.orderProducts") { hasSize<Any>(order.products.size) }
            order.products.forEachIndexed { index, orderProductQueryModel ->
                jsonPath("$.orderProducts[$index].productId") { value(orderProductQueryModel.productId.value) }
                jsonPath("$.orderProducts[$index].quantity") { value(orderProductQueryModel.quantity) }
                jsonPath("$.orderProducts[$index].unitPrice") { value(orderProductQueryModel.unitPrice.toPlainString()) }
                jsonPath("$.orderProducts[$index].totalPrice") { value(orderProductQueryModel.totalPrice.toPlainString()) }
            }
        }
    }

    @Test
    fun `주문 - 400 - 재고 부족`() {
        val request = request()
        every { orderFacade.order(any()) } throws OutOfStockProductException(1L, 1, 0L)

        mockMvc.post("/orders") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value(ErrorCode.OUT_OF_STOCK_PRODUCT.name) }
        }
    }

    @Test
    fun `주문 - 400 - 유효하지 않은 주문 금액`() {
        val request = request()
        every { orderFacade.order(any()) } throws InvalidOrderPriceException(ProductMock.id(), "deatil")

        mockMvc.post("/orders") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value(ErrorCode.INVALID_ORDER_PRICE.name) }
        }
    }

    @Test
    fun `주문 - 400 - 이미 사용된 쿠폰`() {
        val request = request()
        every { orderFacade.order(any()) } throws AlreadyUsedCouponException(CouponMock.id())

        mockMvc.post("/orders") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value(ErrorCode.ALREADY_USED_COUPON.name) }
        }
    }

    @Test
    fun `주문 - 400 - 만료된 쿠폰`() {
        val request = request()
        every { orderFacade.order(any()) } throws ExpiredCouponException(CouponMock.id())

        mockMvc.post("/orders") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value(ErrorCode.EXPIRED_COUPON.name) }
        }
    }

    @Test
    fun `주문 - 400 - 잔고 부족`() {
        val request = request()
        every { orderFacade.order(any()) } throws InsufficientBalanceException(1L, BigDecimal.ZERO, BigDecimal.ZERO)

        mockMvc.post("/orders") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value(ErrorCode.INSUFFICIENT_BALANCE.name) }
        }
    }

    @Test
    fun `주문 - 404 - 찾을 수 없는 유저`() {
        val request = request()
        every { orderFacade.order(any()) } throws NotFoundUserException()

        mockMvc.post("/orders") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.errorCode") { value(ErrorCode.NOT_FOUND_USER.name) }
        }
    }

    @Test
    fun `주문 - 404 - 찾을 수 없는 상품`() {
        val request = request()
        every { orderFacade.order(any()) } throws NotFoundProductException("")

        mockMvc.post("/orders") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.errorCode") { value(ErrorCode.NOT_FOUND_PRODUCT.name) }
        }
    }

    @Test
    fun `주문 - 404 - 찾을 수 없는 쿠폰`() {
        val request = request()
        every { orderFacade.order(any()) } throws NotFoundCouponException("")

        mockMvc.post("/orders") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.errorCode") { value(ErrorCode.NOT_FOUND_COUPON.name) }
        }
    }

    private fun request(): OrderRequest = OrderRequest(
        userId = 1L,
        orderProducts = listOf(
            OrderRequest.OrderProductRequest(
                productId = 1L,
                quantity = 2,
                unitPrice = BigDecimal.valueOf(10_000),
                totalPrice = BigDecimal.valueOf(20_000)
            )
        ),
        couponId = 2L,
        originalAmount = BigDecimal.valueOf(20_000),
        discountAmount = BigDecimal.valueOf(10_000),
        totalAmount = BigDecimal.valueOf(10_000),
    )
}
