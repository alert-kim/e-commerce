package kr.hhplus.be.server.domain.user.exception

import kr.hhplus.be.server.domain.DomainException

abstract class UserException : DomainException()

class NotFoundUserException(
    detail: String = "",
) : UserException() {
    override val message: String = "사용자를 찾을 수 없습니다. $detail"
}

class RequiredUserIdException() : UserException() {
    override val message: String = "사용자 ID가 필요합니다"
}
