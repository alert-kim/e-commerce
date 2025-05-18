package kr.hhplus.be.server.application.order.processor

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.application.order.command.CreateOrderProcessorCommand
import kr.hhplus.be.server.application.order.command.FailOrderProcessorCommand
import kr.hhplus.be.server.application.order.command.MarkOrderFailHandledProcessorCommand
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.command.CreateOrderCommand
import kr.hhplus.be.server.domain.order.command.FailOrderCommand
import kr.hhplus.be.server.domain.order.command.MarkOrderFailHandledCommand
import kr.hhplus.be.server.domain.user.UserService
import kr.hhplus.be.server.testutil.mock.OrderMock
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OrderLifecycleProcessorTest {
    private val userService = mockk<UserService>(relaxed = true)
    private val orderService = mockk<OrderService>(relaxed = true)
    private val processor = OrderLifecycleProcessor(userService, orderService)

    @BeforeEach
    fun setup() {
        clearMocks(userService, orderService)
    }

    @Nested
    @DisplayName("주문 생성 처리")
    inner class CreateOrder {
        @Test
        @DisplayName("사용자 검증 후 주문을 생성한다")
        fun createOrder() {
            val userId = UserMock.id()
            val user = UserMock.view(id = userId)
            val orderId = OrderMock.id()
            every { userService.get(userId.value) } returns user
            every { orderService.createOrder(any<CreateOrderCommand>()) } returns orderId

            val command = CreateOrderProcessorCommand(userId.value)
            val result = processor.createOrder(command)

            assertThat(result.orderId).isEqualTo(orderId)
            verify {
                userService.get(userId.value)
                orderService.createOrder(
                    CreateOrderCommand(
                        userId = userId
                    )
                )
            }
        }
    }

    @Nested
    @DisplayName("주문 실패 처리")
    inner class FailOrder {
        @Test
        @DisplayName("주문 실패 내역을 기록한다")
        fun failOrder() {
            val orderId = OrderMock.id()
            val reason = "테스트 실패 사유"

            processor.failOrder(FailOrderProcessorCommand(orderId, reason))

            verify {
                orderService.failOrder(
                    FailOrderCommand(
                        orderId = orderId,
                        reason = reason
                    )
                )
            }
        }
    }

    @Nested
    @DisplayName("실패 처리 완료 표시")
    inner class MarkFailHandled {
        @Test
        @DisplayName("실패한 주문의 후속 처리가 완료됨을 표시한다")
        fun markFailHandled() {
            val orderId = OrderMock.id()

            processor.markFailHandled(MarkOrderFailHandledProcessorCommand(orderId))

            verify {
                orderService.markFailHandled(MarkOrderFailHandledCommand(orderId))
            }
        }
    }
}
