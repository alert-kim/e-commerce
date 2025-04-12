package kr.hhplus.be.server.domain.common

import org.springframework.data.domain.Sort

abstract class CommonException : RuntimeException()

class InvalidPageRequestArgumentException(
    page: Int,
    pageSize: Int,
    sort: Sort,
) : CommonException() {
    override val message = "페이지 요청(page: $page, size: $pageSize, sort: ${sort})이 유효하지 않음"
}
