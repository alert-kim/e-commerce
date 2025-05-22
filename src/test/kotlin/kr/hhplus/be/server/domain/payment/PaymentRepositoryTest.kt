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

class PaymentRepositoryTest @Autowired constructor(
    private val repository: PaymentRepository
) : RepositoryTest() {

    @Nested
    @DisplayName("저장")
    inner class Save {

        @Test
        @DisplayName("결제 저장 후 필드 확인")
        fun save() {
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
    @DisplayName("ID로 조회")
    inner class FindById {

        @Test
        @DisplayName("존재하는 결제 반환")
        fun findById() {
            val payment = PaymentMock.payment(id = null)
            repository.save(payment)

            val found = repository.findById(payment.id().value)

            assertThat(found).isNotNull
            assertThat(found?.id()).isEqualTo(payment.id())
        }

        @Test
        @DisplayName("존재하지 않는 결제는 null 반환")
        fun findNotExistsById() {
            val found = repository.findById(PaymentMock.id().value)

            assertThat(found).isNull()
        }
    }

    @Nested
    @DisplayName("주문 ID로 조회")
    inner class FindByOrderId {

        @Test
        @DisplayName("존재하는 결제 반환")
        fun findByOrderId() {
            val payment = PaymentMock.payment(id = null)
            repository.save(payment)

            val found = repository.findByOrderId(payment.orderId)

            assertThat(found).isNotNull
            assertThat(found?.id()).isEqualTo(payment.id())
        }

        @Test
        @DisplayName("존재하지 않는 결제는 null 반환")
        fun findNotExistsByOrderId() {
            val found = repository.findByOrderId(OrderMock.id())

            assertThat(found).isNull()
        }
    }
}
