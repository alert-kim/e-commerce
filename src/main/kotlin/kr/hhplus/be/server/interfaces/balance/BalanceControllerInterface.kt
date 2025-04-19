package kr.hhplus.be.server.interfaces.balance

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.server.interfaces.BalanceApiErrorCode.BELOW_MIN_BALANCE_CODE
import kr.hhplus.be.server.interfaces.BalanceApiErrorCode.EXCEED_MAX_BALANCE_CODE
import kr.hhplus.be.server.interfaces.ErrorResponse
import kr.hhplus.be.server.interfaces.balance.request.ChargeApiRequest
import kr.hhplus.be.server.interfaces.balance.response.BalanceResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "Balance API", description = "잔고 조회 및 충전 API")
interface BalanceControllerInterface {

    @Operation(
        summary = "잔고 조회",
        description = "유저 ID를 기반으로 현재 잔고 조회"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "잔고 조회 성공",
                content = [Content(schema = Schema(implementation = BalanceResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "리소스를 찾을 수 없음",
                content = [Content(
                    examples = [ExampleObject(value = """{"code":"NOT_FOUND_USER"}""")],
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            )
        ]
    )
    @GetMapping
    fun getBalance(
        @Parameter(description = "유저 ID", required = true, example = "1")
        @RequestParam userId: Long
    ): ResponseEntity<*>

    @Operation(
        summary = "잔고 충전",
        description = "잔고를 충전함"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "잔고 충전 성공",
                content = [Content(schema = Schema(implementation = BalanceResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                name = "최대 잔고 금액 초과",
                                value = EXCEED_MAX_BALANCE_CODE,
                                summary = "EXCEED_MAX_BALANCE"
                            ),
                            ExampleObject(
                                name = "최소 잔고 금액 미만",
                                value = BELOW_MIN_BALANCE_CODE,
                                summary = "BELOW_MIN_BALANCE"
                            ),
                        ],
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponse::class)
                    ),
                ]
            ),
            ApiResponse(
                responseCode = "404",
                description = "리소스를 찾을 수 없음",
                content = [Content(
                    examples = [ExampleObject(value = """{"code":"NOT_FOUND_USER"}""")],
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            )
        ]
    )
    @PostMapping("/charge")
    fun chargeBalance(
        @RequestBody request: ChargeApiRequest
    ): ResponseEntity<*>
}
