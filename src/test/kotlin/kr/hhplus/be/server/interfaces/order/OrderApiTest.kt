package kr.hhplus.be.server.interfaces.order

import kr.hhplus.be.server.domain.order.OrderStatus
import kr.hhplus.be.server.domain.product.ProductStatus
import kr.hhplus.be.server.interfaces.ApiTest
import kr.hhplus.be.server.interfaces.ErrorCode
import kr.hhplus.be.server.interfaces.order.reqeust.OrderRequest
import kr.hhplus.be.server.testutil.mock.IdMock
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import java.math.BigDecimal
import java.time.Instant

class OrderApiTest : ApiTest() {

    @Test
    @DisplayName("쿠폰을 사용하지 않는 주문 처리")
    fun orderWithoutCoupon() {
        val user = savedUser()
        val products = List(3) {
            savedProduct(
                status = ProductStatus.ON_SALE,
                stock = 100,
                price = BigDecimal.valueOf(10000)
            )
        }
        val orderProducts = products.map {
            OrderRequest.OrderProductRequest(
                productId = it.id().value,
                quantity = 2,
                unitPrice = it.price,
                totalPrice = it.price.multiply(BigDecimal.valueOf(2))
            )
        }
        val request = OrderRequest(
            userId = user.id().value,
            couponId = null,
            orderProducts = orderProducts,
            originalAmount = orderProducts.sumOf { it.totalPrice },
            discountAmount = BigDecimal.ZERO,
            totalAmount = orderProducts.sumOf { it.totalPrice }
        )
        savedBalance(
            userId = user.id(),
            amount = request.totalAmount,
        )

        mockMvc.post("/orders") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { isNumber() }
            jsonPath("$.userId") { value(user.id().value) }
            jsonPath("$.status") { value(OrderStatus.COMPLETED.name) }
            jsonPath("$.couponId") { isEmpty() }
            jsonPath("$.originalAmount") { value(request.originalAmount.toDouble()) }
            jsonPath("$.discountAmount") { value(request.discountAmount.toDouble()) }
            jsonPath("$.totalAmount") { value(request.totalAmount.toDouble()) }
            jsonPath("$.createdAt") { isNotEmpty() }
            jsonPath("$.orderProducts", hasSize<Any>(products.size))
            request.orderProducts.forEachIndexed { index, req ->
                jsonPath("$.orderProducts[$index].productId") { value(req.productId) }
                jsonPath("$.orderProducts[$index].quantity") { value(req.quantity) }
                jsonPath("$.orderProducts[$index].unitPrice") { value(req.unitPrice.toDouble()) }
                jsonPath("$.orderProducts[$index].totalPrice") { value(req.totalPrice.toDouble()) }
            }
        }
    }

    @Test
    @DisplayName("쿠폰을 사용하는 주문 처리")
    fun orderWithCoupon() {
        val user = savedUser()
        val products = List(3) {
            savedProduct(
                status = ProductStatus.ON_SALE,
                stock = 100,
                price = BigDecimal.valueOf(10000)
            )
        }
        val orderProducts = products.map {
            OrderRequest.OrderProductRequest(
                productId = it.id().value,
                quantity = 2,
                unitPrice = it.price,
                totalPrice = it.price.multiply(BigDecimal.valueOf(2))
            )
        }
        val request = OrderRequest(
            userId = user.id().value,
            couponId = savedCoupon(user.id(), discountAmount = 1000.toBigDecimal(), usedAt = null).id().value,
            orderProducts = orderProducts,
            originalAmount = orderProducts.sumOf { it.totalPrice },
            discountAmount = 1000.toBigDecimal(),
            totalAmount = orderProducts.sumOf { it.totalPrice } - 1000.toBigDecimal()
        )
        savedBalance(
            userId = user.id(),
            amount = request.totalAmount,
        )

        mockMvc.post("/orders") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { isNumber() }
            jsonPath("$.userId") { value(user.id().value) }
            jsonPath("$.status") { value(OrderStatus.COMPLETED.name) }
            jsonPath("$.couponId") { value(request.couponId) }
            jsonPath("$.originalAmount") { value(request.originalAmount.toDouble()) }
            jsonPath("$.discountAmount") { value(request.discountAmount.toDouble()) }
            jsonPath("$.totalAmount") { value(request.totalAmount.toDouble()) }
            jsonPath("$.createdAt") { isNotEmpty() }
            jsonPath("$.orderProducts", hasSize<Any>(products.size))
            request.orderProducts.forEachIndexed { index, req ->
                jsonPath("$.orderProducts[$index].productId") { value(req.productId) }
                jsonPath("$.orderProducts[$index].quantity") { value(req.quantity) }
                jsonPath("$.orderProducts[$index].unitPrice") { value(req.unitPrice.toDouble()) }
                jsonPath("$.orderProducts[$index].totalPrice") { value(req.totalPrice.toDouble()) }
            }
        }
    }

    @Test
    @DisplayName("재고 부족 시 400 오류 응답")
    fun insufficientStock() {
        val user = savedUser()
        val products = List(1) {
            savedProduct(
                status = ProductStatus.ON_SALE,
                stock = 10,
                price = BigDecimal.valueOf(1000)
            )
        }
        val orderProducts = products.map {
            OrderRequest.OrderProductRequest(
                productId = it.id().value,
                quantity = 20,
                unitPrice = it.price,
                totalPrice = it.price.multiply(BigDecimal.valueOf(20))
            )
        }
        val request = OrderRequest(
            userId = user.id().value,
            couponId = null,
            orderProducts = orderProducts,
            originalAmount = orderProducts.sumOf { it.totalPrice },
            discountAmount = BigDecimal.ZERO,
            totalAmount = orderProducts.sumOf { it.totalPrice },
        )
        savedBalance(
            userId = user.id(),
            amount = request.totalAmount,
        )

        mockMvc.post("/orders") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value(ErrorCode.OUT_OF_STOCK_PRODUCT.name) }
        }
    }

    @Test
    @DisplayName("유효하지 않은 주문 금액 시 400 오류 응답")
    fun invalidOrderAmount() {
        val user = savedUser()
        val products = List(1) {
            savedProduct(
                status = ProductStatus.ON_SALE,
                stock = 10,
                price = BigDecimal.valueOf(1000)
            )
        }
        val orderProducts = products.map {
            OrderRequest.OrderProductRequest(
                productId = it.id().value,
                quantity = 1,
                unitPrice = it.price,
                totalPrice = it.price.plus(BigDecimal.ONE)
            )
        }
        val request = OrderRequest(
            userId = user.id().value,
            couponId = null,
            orderProducts = orderProducts,
            originalAmount = orderProducts.sumOf { it.totalPrice },
            discountAmount = BigDecimal.ZERO,
            totalAmount = orderProducts.sumOf { it.totalPrice },
        )
        savedBalance(
            userId = user.id(),
            amount = request.totalAmount,
        )

        mockMvc.post("/orders") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value(ErrorCode.INVALID_ORDER_PRICE.name) }
        }
    }

    @Test
    @DisplayName("유효하지 않은 주문 수량 시 400 오류 응답")
    fun invalidOrderQuantity() {
        val user = savedUser()
        val products = List(1) {
            savedProduct(
                status = ProductStatus.ON_SALE,
                stock = 10,
                price = BigDecimal.valueOf(1000)
            )
        }
        val orderProducts = products.map {
            OrderRequest.OrderProductRequest(
                productId = it.id().value,
                quantity = -1,
                unitPrice = it.price,
                totalPrice = it.price,
            )
        }
        val request = OrderRequest(
            userId = user.id().value,
            couponId = null,
            orderProducts = orderProducts,
            originalAmount = orderProducts.sumOf { it.totalPrice },
            discountAmount = BigDecimal.ZERO,
            totalAmount = orderProducts.sumOf { it.totalPrice },
        )
        savedBalance(
            userId = user.id(),
            amount = request.totalAmount,
        )

        mockMvc.post("/orders") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value(ErrorCode.INVALID_ORDER_QUANTITY.name) }
        }
    }

    @Test
    @DisplayName("잔고 부족 시 400 오류 응답")
    fun insufficientBalance() {
        val user = savedUser()
        val products = List(1) {
            savedProduct(
                status = ProductStatus.ON_SALE,
                stock = 10,
                price = BigDecimal.valueOf(1000)
            )
        }
        val orderProducts = products.map {
            OrderRequest.OrderProductRequest(
                productId = it.id().value,
                quantity = 1,
                unitPrice = it.price,
                totalPrice = it.price,
            )
        }
        val request = OrderRequest(
            userId = user.id().value,
            couponId = null,
            orderProducts = orderProducts,
            originalAmount = orderProducts.sumOf { it.totalPrice },
            discountAmount = BigDecimal.ZERO,
            totalAmount = orderProducts.sumOf { it.totalPrice },
        )
        savedBalance(
            userId = user.id(),
            amount = request.totalAmount.minus(BigDecimal.ONE),
        )

        mockMvc.post("/orders") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value(ErrorCode.INSUFFICIENT_BALANCE.name) }
        }
    }

    @Test
    @DisplayName("이미 사용된 쿠폰 사용 시 400 오류 응답")
    fun alreadyUsedCoupon() {
        val user = savedUser()
        val products = List(1) {
            savedProduct(
                status = ProductStatus.ON_SALE,
                stock = 100,
                price = BigDecimal.valueOf(10000)
            )
        }
        val orderProducts = products.map {
            OrderRequest.OrderProductRequest(
                productId = it.id().value,
                quantity = 1,
                unitPrice = it.price,
                totalPrice = it.price,
            )
        }
        val request = OrderRequest(
            userId = user.id().value,
            couponId = savedCoupon(user.id(), discountAmount = 1000.toBigDecimal(), usedAt = Instant.now()).id().value,
            orderProducts = orderProducts,
            originalAmount = orderProducts.sumOf { it.totalPrice },
            discountAmount = 1000.toBigDecimal(),
            totalAmount = orderProducts.sumOf { it.totalPrice } - 1000.toBigDecimal()
        )
        savedBalance(
            userId = user.id(),
            amount = request.totalAmount,
        )

        mockMvc.post("/orders") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value(ErrorCode.ALREADY_USED_COUPON.name) }
        }
    }

    @Test
    @DisplayName("존재하지 않는 유저로 주문 시 404 오류 응답")
    fun userNotFound() {
        val products = List(1) {
            savedProduct(
                status = ProductStatus.ON_SALE,
                stock = 100,
                price = BigDecimal.valueOf(10000)
            )
        }
        val orderProducts = products.map {
            OrderRequest.OrderProductRequest(
                productId = it.id().value,
                quantity = 1,
                unitPrice = it.price,
                totalPrice = it.price,
            )
        }
        val request = OrderRequest(
            userId = IdMock.value(),
            couponId = null,
            orderProducts = orderProducts,
            originalAmount = orderProducts.sumOf { it.totalPrice },
            discountAmount = BigDecimal.ZERO,
            totalAmount = orderProducts.sumOf { it.totalPrice },
        )

        mockMvc.post("/orders") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.errorCode") { value(ErrorCode.NOT_FOUND_USER.name) }
        }
    }

    @Test
    @DisplayName("존재하지 않는 상품으로 주문 시 404 오류 응답")
    fun productNotFound() {
        val user = savedUser()
        val orderProducts = listOf(
            OrderRequest.OrderProductRequest(
                productId = IdMock.value(),
                quantity = 1,
                unitPrice = 1000.toBigDecimal(),
                totalPrice = 1000.toBigDecimal()
            )
        )
        val request = OrderRequest(
            userId = user.id().value,
            couponId = null,
            orderProducts = orderProducts,
            originalAmount = orderProducts.sumOf { it.totalPrice },
            discountAmount = BigDecimal.ZERO,
            totalAmount = orderProducts.sumOf { it.totalPrice },
        )
        savedBalance(
            userId = user.id(),
            amount = request.totalAmount,
        )

        mockMvc.post("/orders") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.errorCode") { value(ErrorCode.NOT_FOUND_PRODUCT.name) }
        }
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰으로 주문 시 404 오류 응답")
    fun couponNotFound() {
        val user = savedUser()
        val products = List(1) {
            savedProduct(
                status = ProductStatus.ON_SALE,
                stock = 100,
                price = BigDecimal.valueOf(10000)
            )
        }
        val orderProducts = products.map {
            OrderRequest.OrderProductRequest(
                productId = it.id().value,
                quantity = 1,
                unitPrice = it.price,
                totalPrice = it.price,
            )
        }
        val request = OrderRequest(
            userId = user.id().value,
            couponId = IdMock.value(),
            orderProducts = orderProducts,
            originalAmount = orderProducts.sumOf { it.totalPrice },
            discountAmount = 1000.toBigDecimal(),
            totalAmount = orderProducts.sumOf { it.totalPrice } - 1000.toBigDecimal()
        )
        savedBalance(
            userId = user.id(),
            amount = request.totalAmount,
        )

        mockMvc.post("/orders") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.errorCode") { value(ErrorCode.NOT_FOUND_COUPON.name) }
        }
    }
}
