package kr.hhplus.be.server.domain.order

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import kr.hhplus.be.server.domain.coupon.CouponId
import kr.hhplus.be.server.domain.order.exception.AlreadyCouponAppliedException
import kr.hhplus.be.server.domain.order.exception.InvalidOrderStatusException
import kr.hhplus.be.server.domain.order.exception.RequiredOrderIdException
import kr.hhplus.be.server.domain.product.ProductPrice
import kr.hhplus.be.server.testutil.mock.CouponMock
import kr.hhplus.be.server.testutil.mock.OrderMock
import kr.hhplus.be.server.testutil.mock.ProductMock
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.math.BigDecimal
import java.time.Instant

class OrderTest {
    
    @Nested
    @DisplayName("Id 조회")
    inner class Id {
        @Test
        @DisplayName("Id가 있으면 OrderId를 반환한다")
        fun returnId() {
            val id = OrderMock.id()
            val order = OrderMock.order(id = id)

            val result = order.id()

            assertThat(result).isEqualTo(id)
        }

        @Test
        @DisplayName("Id가 null이면 예외가 발생한다")
        fun throwException() {
            val order = OrderMock.order(id = null)

            assertThrows<RequiredOrderIdException> {
                order.id()
            }
        }
    }
    
    @Nested
    @DisplayName("주문 생성")
    inner class New {
        @Test
        @DisplayName("새 주문은 READY 상태와 0원으로 초기화된다")
        fun createNew() {
            val userId = UserMock.id()

            val order = Order.new(userId)

            assertAll(
                { assertThat(order.userId).isEqualTo(userId) },
                { assertThat(order.status).isEqualTo(OrderStatus.READY) },
                { assertThat(order.originalAmount).isEqualByComparingTo(BigDecimal.ZERO) },
                { assertThat(order.discountAmount).isEqualByComparingTo(BigDecimal.ZERO) },
                { assertThat(order.totalAmount).isEqualByComparingTo(BigDecimal.ZERO) },
                { assertThat(order.products).isEmpty() },
                { assertThat(order.couponId).isNull() },
            )
        }
    }
    
    @Nested
    @DisplayName("상품 재고 배치")
    inner class PlaceStock {
        @Test
        @DisplayName("주문에 상품을 추가하고 금액을 계산한다")
        fun addProduct() {
            val order = OrderMock.order(
                status = OrderStatus.READY,
                products = emptyList(),
                originalAmount = BigDecimal.ZERO,
                discountAmount = BigDecimal.ZERO,
                totalAmount = BigDecimal.ZERO,
            )
            val productId = ProductMock.id()
            val quantity = Arb.int(1..5).next()
            val unitPrice = ProductPrice(1000.toBigDecimal())
            val totalAmount = unitPrice.value.multiply(BigDecimal.valueOf(quantity.toLong()))


            val resultOrderProduct = order.placeStock(productId, quantity, unitPrice)

            assertThat(resultOrderProduct.orderId()).isEqualTo(order.id())
            assertThat(resultOrderProduct.productId).isEqualTo(productId)
            assertThat(resultOrderProduct.quantity).isEqualTo(quantity)
            assertThat(resultOrderProduct.unitPrice).isEqualByComparingTo(unitPrice.value)
            assertAll(
                { assertThat(order.products).hasSize(1) },
                { assertThat(order.originalAmount).isEqualByComparingTo(totalAmount) },
                { assertThat(order.totalAmount).isEqualByComparingTo(totalAmount) },
                { assertThat(order.products[0].productId).isEqualTo(productId) },
                { assertThat(order.products[0].quantity).isEqualTo(quantity) },
                { assertThat(order.products[0].unitPrice).isEqualByComparingTo(unitPrice.value) },
            )
        }

        @Test
        @DisplayName("READY 상태가 아니면 예외가 발생한다")
        fun invalidStatus() {
            val order = OrderMock.order(
                status = OrderStatus.COMPLETED,
            )

            assertThrows<InvalidOrderStatusException> {
                order.placeStock(ProductMock.id(), 1, ProductPrice(1000.toBigDecimal()))
            }
        }
    }
    
    @Nested
    @DisplayName("쿠폰 적용")
    inner class ApplyCoupon {
        @Test
        @DisplayName("쿠폰을 적용하고 할인 금액을 계산한다")
        fun apply() {
            val updatedAt = Instant.now()
            val originalAmount = BigDecimal.valueOf(1_500)
            val discountAmount = BigDecimal.valueOf(500)
            val expectTotalAmount = BigDecimal.valueOf(1_000)
            val order = OrderMock.order(
                status = OrderStatus.STOCK_ALLOCATED,
                couponId = null,
                totalAmount = originalAmount,
                originalAmount = originalAmount,
                discountAmount = BigDecimal.ZERO,
                updatedAt = updatedAt,
            )
            val couponId = CouponMock.id()
            val usedCoupon = CouponMock.usedCoupon(
                id = couponId,
                discountAmount = discountAmount,
            )

            order.applyCoupon(usedCoupon)

            assertAll(
                { assertThat(order.couponId).isEqualTo(couponId) },
                { assertThat(order.status).isEqualTo(OrderStatus.STOCK_ALLOCATED) },
                { assertThat(order.originalAmount).isEqualByComparingTo(order.originalAmount) },
                { assertThat(order.discountAmount).isEqualByComparingTo(discountAmount) },
                { assertThat(order.totalAmount).isEqualByComparingTo(expectTotalAmount) },
                { assertThat(order.updatedAt).isAfter(updatedAt) },
            )
        }

        @Test
        @DisplayName("같은 쿠폰이 이미 적용되어 있으면 재적용하지 않는다")
        fun sameCoupon() {
            val couponId = CouponId(1L)
            val usedCoupon = CouponMock.usedCoupon(
                id = couponId,
            )
            val updatedAt = Instant.now()
            val order = OrderMock.order(
                status = OrderStatus.STOCK_ALLOCATED,
                couponId = couponId,
                updatedAt = updatedAt,
            )

            order.applyCoupon(usedCoupon)

            assertAll(
                { assertThat(order.couponId).isEqualTo(couponId) },
                { assertThat(order.discountAmount).isEqualByComparingTo(order.discountAmount) },
                { assertThat(order.totalAmount).isEqualByComparingTo(order.totalAmount) },
            )
        }

        @Test
        @DisplayName("다른 쿠폰이 이미 적용되어 있으면 예외가 발생한다")
        fun differentCoupon() {
            val couponId = CouponId(2L)
            val usedCoupon = CouponMock.usedCoupon(
                id = couponId,
            )
            val order = OrderMock.order(
                status = OrderStatus.STOCK_ALLOCATED,
                couponId = CouponId(1L),
            )

            shouldThrow<AlreadyCouponAppliedException> {
                order.applyCoupon(usedCoupon)
            }
        }

        @Test
        @DisplayName("STOCK_ALLOCATED 상태가 아니면 예외가 발생한다")
        fun invalidStatus() {
            val usedCoupon = CouponMock.usedCoupon(
                id = CouponMock.id(),
            )
            val order = OrderMock.order(
                status = OrderStatus.COMPLETED,
            )

            assertThrows<InvalidOrderStatusException> {
                order.applyCoupon(usedCoupon)
            }
        }
    }
    
    @Nested
    @DisplayName("결제 처리")
    inner class Pay {
        @Test
        @DisplayName("주문 상태를 COMPLETED로 변경한다")
        fun complete() {
            val order = OrderMock.order(
                status = OrderStatus.STOCK_ALLOCATED,
            )

            order.pay()

            assertAll(
                { assertThat(order.status).isEqualTo(OrderStatus.COMPLETED) },
                { assertThat(order.updatedAt).isAfter(order.createdAt) },
            )
        }

        @Test
        @DisplayName("STOCK_ALLOCATED 상태가 아니면 예외가 발생한다")
        fun invalidStatus() {
            val order = OrderMock.order(
                status = OrderStatus.READY,
            )

            assertThrows<InvalidOrderStatusException> {
                order.pay()
            }
        }
    }
    
    @Nested
    @DisplayName("실패 처리")
    inner class Fail {
        @Test
        @DisplayName("주문 상태를 FAILED로 변경한다")
        fun fail() {
            val order = OrderMock.order(status = OrderStatus.READY)
            val beforeTime = order.updatedAt
            
            order.fail()
            
            assertThat(order.status).isEqualTo(OrderStatus.FAILED)
            assertThat(order.updatedAt).isAfter(beforeTime)
        }
    }

    @Nested
    @DisplayName("실패 처리됨 표시")
    inner class FailHandled {
        @Test
        @DisplayName("실패 상태인 주문을 실패 처리됨으로 변경한다")
        fun failHandled() {
            val order = OrderMock.order(status = OrderStatus.FAILED)
            val beforeTime = order.updatedAt

            order.failHandled()

            assertThat(order.status).isEqualTo(OrderStatus.FAIL_HANDLED)
            assertThat(order.updatedAt).isAfter(beforeTime)
        }

        @Test
        @DisplayName("실패 상태가 아닌 주문을 실패 처리됨으로 변경하면 예외가 발생한다")
        fun notFailed() {
            val status = OrderStatus.entries.filter { it != OrderStatus.FAILED }.random()
            val order = OrderMock.order(status = status)

            assertThrows<InvalidOrderStatusException> {
                order.failHandled()
            }
        }
    }

    @Nested
    @DisplayName("실패 처리")
    inner class IsFailed {
        @Test
        @DisplayName("실패 상태라면 true를 반환한다")
        fun isFailed() {
            val failedOrder = OrderMock.order(status = OrderStatus.FAILED)

            assertThat(failedOrder.isFailed()).isTrue()
        }

        @Test
        @DisplayName("실패 상태가 아니라면 false를 반환한다")
        fun isNotFailed() {
            val status = OrderStatus.entries.filter { it != OrderStatus.FAILED }.random()
            val order = OrderMock.order(status = status)

            assertThat(order.isFailed()).isFalse()
        }
    }
}
