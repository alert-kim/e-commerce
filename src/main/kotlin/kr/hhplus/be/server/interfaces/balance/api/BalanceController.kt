package kr.hhplus.be.server.interfaces.balance.api

import kr.hhplus.be.server.application.balance.BalanceFacade
import kr.hhplus.be.server.application.balance.command.ChargeBalanceFacadeCommand
import kr.hhplus.be.server.domain.balance.exception.BelowMinBalanceAmountException
import kr.hhplus.be.server.domain.balance.exception.ExceedMaxBalanceAmountException
import kr.hhplus.be.server.domain.user.exception.NotFoundUserException
import kr.hhplus.be.server.interfaces.ErrorCode
import kr.hhplus.be.server.interfaces.ErrorSpec
import kr.hhplus.be.server.interfaces.balance.api.request.ChargeApiRequest
import kr.hhplus.be.server.interfaces.balance.api.response.BalanceResponse
import kr.hhplus.be.server.interfaces.common.api.ServerApiResponse
import kr.hhplus.be.server.interfaces.common.api.handleRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/balances")
class BalanceController(
    private val balanceFacade: BalanceFacade,
) : BalanceControllerInterface {

    @GetMapping
    override fun getBalance(
        @RequestParam userId: Long,
    ): ResponseEntity<ServerApiResponse> = handleRequest(
        block = {
            val balance = balanceFacade.getOrNullByUerId(userId)
            BalanceResponse.Companion.of(balance)
        },
        errorSpec = {
            when (it) {
                is NotFoundUserException -> ErrorSpec.notFound(ErrorCode.NOT_FOUND_USER)
                else -> ErrorSpec.serverError(ErrorCode.INTERNAL_SERVER_ERROR)
            }
        }
    )

    @PostMapping("/charge")
    override fun chargeBalance(
        @RequestBody request: ChargeApiRequest,
    ) = handleRequest(
        block = {
            val chargedBalance = balanceFacade.charge(
                ChargeBalanceFacadeCommand(
                    userId = request.userId,
                    amount = request.amount,
                )
            )
            BalanceResponse.Companion.of(chargedBalance)
        },
        errorSpec = {
            when (it) {
                is NotFoundUserException -> ErrorSpec.notFound(ErrorCode.NOT_FOUND_USER)
                is ExceedMaxBalanceAmountException -> ErrorSpec.badRequest(ErrorCode.EXCEED_MAX_BALANCE)
                is BelowMinBalanceAmountException -> ErrorSpec.badRequest(ErrorCode.BELOW_MIN_BALANCE)
                else -> ErrorSpec.serverError(ErrorCode.INTERNAL_SERVER_ERROR)
            }
        }
    )
}
