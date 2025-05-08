package kr.hhplus.be.server.domain.product.excpetion

import kr.hhplus.be.server.domain.DomainException

abstract class ProductStatException : DomainException()

class RequiredProductStatIdException() : ProductStatException() {
    override val message: String = "상품 판매 통계 ID가 필요합니다."
}



