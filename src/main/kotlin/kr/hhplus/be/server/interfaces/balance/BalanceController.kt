package kr.hhplus.be.server.interfaces.balance

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.server.interfaces.BalanceApiErrorCode.EXCEED_MAX_BALANCE_CODE
import kr.hhplus.be.server.interfaces.BalanceApiErrorCode.NOT_FOUND_BALANCE_CODE
import kr.hhplus.be.server.interfaces.ErrorResponse
import kr.hhplus.be.server.interfaces.UserApiErrorCode.NOT_FOUND_USER_CODE
import kr.hhplus.be.server.interfaces.balance.request.ChargeApiRequest
import kr.hhplus.be.server.interfaces.balance.response.UserBalanceResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/balances")
@Tag(name = "Balance API", description = "잔고 조회 및 충전 API")
class BalanceController {

    @Operation(
        summary = "잔고 조회",
        description = "유저 ID를 기반으로 현재 잔고 조회"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "잔고 조회 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(
                            implementation = UserBalanceResponse::class,
                            description = "잔고 조회 성공"
                        )
                    )
                ]
            ),
            ApiResponse(
                responseCode = "404",
                description = "리소스를 찾을 수 없음",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                name = "찾을 수 없는 유저",
                                value = NOT_FOUND_USER_CODE,
                                summary = "NOT_FOUND_USER"
                            ),
                        ],
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponse::class)
                    ),
                ]
            ),
        ]
    )
    @GetMapping
    fun getUserBalance(
        @Parameter(description = "유저 ID", required = true, example = "1")
        @RequestParam userId: Long,
    ): UserBalanceResponse =
        UserBalanceResponse(
            id = 1L,
            userId = userId,
            balance = 1000.0.toBigDecimal(),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

    @Operation(
        summary = "잔고 충전",
        description = "잔고를 충전함"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "잔고 충전 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(
                            implementation = UserBalanceResponse::class,
                            description = "잔고 조회 성공"
                        )
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                name = "최대 잔고 초과",
                                value = EXCEED_MAX_BALANCE_CODE,
                                summary = "EXCEED_MAX_BALANCE"
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
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                name = "찾을 수 없는 잔고",
                                value = NOT_FOUND_BALANCE_CODE,
                                summary = "NOT_FOUND_BALANCE"
                            ),
                        ],
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponse::class)
                    ),
                ]
            ),
        ]
    )
    @PostMapping("/{balanceId}:charge")
    fun charge(
        @PathVariable balanceId: Long,
        @Parameter(description = "잔고 ID", required = true, example = "1")
        @RequestBody request: ChargeApiRequest,
    ): UserBalanceResponse =
        UserBalanceResponse(
            id = 1L,
            userId = 2L,
            balance = 1000.0.toBigDecimal(),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
}
