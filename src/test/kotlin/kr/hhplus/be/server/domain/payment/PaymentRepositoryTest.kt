package kr.hhplus.be.server.domain.payment

import io.kotest.assertions.throwables.shouldNotThrowAny
import kr.hhplus.be.server.domain.RepositoryTest
import kr.hhplus.be.server.domain.payment.repository.PaymentRepository
import kr.hhplus.be.server.mock.PaymentMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

class PaymentRepositoryTest : RepositoryTest() {

    @Autowired
    lateinit var repository: PaymentRepository

    @Test
    fun `save - 반환된 Payment 필드 확인`() {
        val payment = PaymentMock.payment(id = null)

        val saved = repository.save(payment)

        shouldNotThrowAny {
            saved.id()
        }
        assertThat(saved.userId).isEqualTo(payment.userId)
        assertThat(saved.orderId).isEqualTo(payment.orderId)
        assertThat(saved.amount).isEqualByComparingTo(payment.amount)
        assertThat(saved.createdAt).isEqualTo(payment.createdAt)
    }
} 
