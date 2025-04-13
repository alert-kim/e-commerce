package kr.hhplus.be.server.domain.payment

import kr.hhplus.be.server.domain.payment.command.PayCommand
import kr.hhplus.be.server.domain.payment.result.PayResult
import org.springframework.stereotype.Service

@Service
class PaymentService {
    fun pay(command: PayCommand): PayResult {
        TODO("Not yet implemented")
    }

}
