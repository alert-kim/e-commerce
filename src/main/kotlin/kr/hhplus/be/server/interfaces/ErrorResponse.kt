package kr.hhplus.be.server.interfaces

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.server.interfaces.common.api.ServerApiResponse

@Schema(description = "에러 응답")
data class ErrorResponse(
    val errorCode: String,
) : ServerApiResponse
