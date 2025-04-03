package kr.hhplus.be.server.controller

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "에러 응답")
data class ErrorResponse(
    val code: String,
)
