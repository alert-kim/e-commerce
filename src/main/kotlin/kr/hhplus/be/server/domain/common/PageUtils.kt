package kr.hhplus.be.server.domain.common

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

fun createPageRequest(
    page: Int,
    pageSize: Int,
    sort: Sort,
): PageRequest =
    runCatching {
        PageRequest.of(page, pageSize, sort)
    }.getOrElse {
        if(it is IllegalArgumentException) {
            throw InvalidPageRequestArgumentException(
                page = page, pageSize = pageSize, sort = sort,
            )
        }
        else throw it
    }
