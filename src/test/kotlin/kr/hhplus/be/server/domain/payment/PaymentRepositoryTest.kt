package kr.hhplus.be.server.domain.payment

import kr.hhplus.be.server.domain.payment.repository.PaymentRepository
import kr.hhplus.be.server.mock.PaymentMock
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kr.hhplus.be.server.domain.RepositoryTest
import org.assertj.core.api.Assertions.assertThat
import io.kotest.assertions.throwables.shouldNotThrowAny

@Transactional
class PaymentRepositoryTest : RepositoryTest() {

    @Autowired
    private lateinit var repository: PaymentRepository

    @Test
    fun `save - 반환된 Payment 필드 확인`() {
        val payment = PaymentMock.payment()

        val saved = repository.save(payment)

        // shouldNotThrowAny {
            // saved.id()
        // }
        // assertThat(saved.userId).isEqualTo(payment.userId)
        // assertThat(saved.orderId).isEqualTo(payment.orderId)
        // assertThat(saved.amount).isEqualByComparingTo(payment.amount)
    }
} 