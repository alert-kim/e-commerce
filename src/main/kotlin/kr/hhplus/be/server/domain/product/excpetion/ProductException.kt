package kr.hhplus.be.server.domain.product.excpetion

import kr.hhplus.be.server.domain.DomainException
import kr.hhplus.be.server.domain.balance.BalanceId
import java.math.BigDecimal

abstract class ProductException : DomainException()

class RequiredProductIdException : ProductException() {
    override val message = "상품 Id가 필요합니다"
}

