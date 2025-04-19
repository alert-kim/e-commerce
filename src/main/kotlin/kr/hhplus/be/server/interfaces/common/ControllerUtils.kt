package kr.hhplus.be.server.interfaces.common

import org.springframework.http.ResponseEntity

fun handleRequest(
    block: () -> ServerApiResponse,
    errorSpec: (Throwable) -> ResponseEntity<ServerApiResponse>,
): ResponseEntity<ServerApiResponse> =
    runCatching {
        val response = block()
        ResponseEntity.ok(response)
    }.getOrElse {
        errorSpec(it)
    }


