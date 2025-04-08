package kr.hhplus.be.server.interfaces.balance

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.server.domain.balance.BalanceService
import kr.hhplus.be.server.domain.user.exception.NotFoundUserException
import kr.hhplus.be.server.domain.user.UserService
import kr.hhplus.be.server.interfaces.BalanceApiErrorCode.EXCEED_MAX_BALANCE_CODE
import kr.hhplus.be.server.interfaces.ErrorCode
import kr.hhplus.be.server.interfaces.ErrorResponse
import kr.hhplus.be.server.interfaces.ErrorSpec
import kr.hhplus.be.server.interfaces.balance.request.ChargeApiRequest
import kr.hhplus.be.server.interfaces.balance.response.BalanceResponse
import kr.hhplus.be.server.interfaces.common.ServerApiResponse
import kr.hhplus.be.server.interfaces.common.handleRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/balances")
@Tag(name = "Balance API", description = "잔고 조회 및 충전 API")
class BalanceController(
    private val userService: UserService,
    private val balanceService: BalanceService,
) {

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
                            implementation = BalanceResponse::class,
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
                                value = """{"code":NOT_FOUND_USER}""",
                                summary = "NOT_FOUND_USER",
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
    fun getBalanceByUser(
        @Parameter(description = "유저 ID", required = true, example = "1")
        @RequestParam userId: Long,
    ): ResponseEntity<ServerApiResponse> = handleRequest(
        block = {
            val user = userService.get(userId)
            val balance = balanceService.getOrNullByUerId(user.id)
            BalanceResponse.of(user.id, balance)
        },
        errorSpec = {
            when(it) {
                is NotFoundUserException -> ErrorSpec.notFound(ErrorCode.NOT_FOUND_USER)
                else -> ErrorSpec.serverError(ErrorCode.INTERNAL_SERVER_ERROR)
            }
        }
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
                            implementation = BalanceResponse::class,
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
                                value = """{"code":NOT_FOUND_BALANCE}""",
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
    ): BalanceResponse =
        BalanceResponse(
            userId = 2L,
            amount = 1000.0.toBigDecimal(),
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )
}
