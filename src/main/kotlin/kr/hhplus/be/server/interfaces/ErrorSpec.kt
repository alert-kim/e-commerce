package kr.hhplus.be.server.interfaces

import kr.hhplus.be.server.interfaces.common.ServerApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

object ErrorSpec {

    fun badRequest(code: ErrorCode): ResponseEntity<ServerApiResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(code.name))

    fun notFound(code: ErrorCode): ResponseEntity<ServerApiResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(code.name))

    fun serverError(code: ErrorCode): ResponseEntity<ServerApiResponse> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(code.name))

}
