package kr.hhplus.be.server.domain.payment

import io.kotest.assertions.throwables.shouldNotThrowAny
import kr.hhplus.be.server.domain.RepositoryTest
import kr.hhplus.be.server.domain.payment.repository.PaymentRepository
import kr.hhplus.be.server.testutil.mock.OrderMock
import kr.hhplus.be.server.testutil.mock.PaymentMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class PaymentRepositoryTest : RepositoryTest() {

    @Autowired
    lateinit var repository: PaymentRepository

    @Nested
    @DisplayName("save")
    inner class Save {
        @Test
        fun `반환된 Payment 필드 확인`() {
            val payment = PaymentMock.payment(id = null)

            val saved = repository.save(payment)

            shouldNotThrowAny {
                saved.id()
            }
            assertThat(saved.userId).isEqualTo(payment.userId)
            assertThat(saved.status).isEqualTo(payment.status)
            assertThat(saved.orderId).isEqualTo(payment.orderId)
            assertThat(saved.amount).isEqualByComparingTo(payment.amount)
            assertThat(saved.canceledAt).isEqualTo(payment.canceledAt)
            assertThat(saved.createdAt).isEqualTo(payment.createdAt)
            assertThat(saved.updatedAt).isEqualTo(payment.updatedAt)
        }
    }

    @Nested
    @DisplayName("findById")
    inner class FindById {
        @Test
        fun `결제가 존재하는 경우 해당 결제 반환`() {
            val payment = PaymentMock.payment(id = null)
            repository.save(payment)

            val found = repository.findById(payment.id().value)

            assertThat(found).isNotNull
            assertThat(found?.id()).isEqualTo(payment.id())
        }

        @Test
        fun `결제 정보가 존재하지 않는 경우 null 반환`() {
            val found = repository.findById(PaymentMock.id().value)

            assertThat(found).isNull()
        }
    }

    @Nested
    @DisplayName("findByOrderId")
    inner class FindByOrderId {
        @Test
        fun `결제가 존재하는 경우 해당 결제 반환`() {
            val payment = PaymentMock.payment(id = null)
            repository.save(payment)

            val found = repository.findByOrderId(payment.orderId)

            assertThat(found).isNotNull
            assertThat(found?.id()).isEqualTo(payment.id())
        }

        @Test
        fun `결제 정보가 존재하지 않는 경우 null 반환`() {
            val found = repository.findByOrderId(OrderMock.id())

            assertThat(found).isNull()
        }
    }
}
