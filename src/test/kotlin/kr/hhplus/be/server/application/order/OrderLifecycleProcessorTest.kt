package kr.hhplus.be.server.application.order

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kr.hhplus.be.server.application.order.command.CreateOrderProcessorCommand
import kr.hhplus.be.server.application.order.command.FailOrderProcessorCommand
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.command.CreateOrderCommand
import kr.hhplus.be.server.domain.order.command.FailOrderCommand
import kr.hhplus.be.server.domain.user.UserService
import kr.hhplus.be.server.testutil.mock.OrderMock
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith


@ExtendWith(MockKExtension::class)
class OrderLifecycleProcessorTest {

    @InjectMockKs
    private lateinit var processor: OrderLifecycleProcessor

    @MockK(relaxed = true)
    private lateinit var userService: UserService

    @MockK(relaxed = true)
    private lateinit var orderService: OrderService

    @Nested
    @DisplayName("주문 생성 처리")
    inner class CreateOrder {
        @Test
        @DisplayName("사용자 검증 후 주문 생성")
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
        @DisplayName("주문 실패 처리")
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

}
