package kr.hhplus.be.server.testutil.assertion

import kr.hhplus.be.server.domain.balance.Balance
import org.assertj.core.api.AbstractAssert


class BalanceAssert(actual: Balance?) : AbstractAssert<BalanceAssert, Balance>(actual, BalanceAssert::class.java) {

    fun isEqualTo(expected: Balance): BalanceAssert {
        when {
            actual == null -> failWithMessage("Balance is null")
            actual.id() != expected.id() -> failWithMessage("Balance id is not equal to expected")
            actual.userId != expected.userId -> failWithMessage("Balance userId is not equal to expected")
            actual.amount.value.compareTo(expected.amount.value) != 0 -> failWithMessage("Balance amount is not equal to expected")
            actual.createdAt != expected.createdAt -> failWithMessage("Balance createdAt is not equal to expected")
            actual.updatedAt != expected.updatedAt -> failWithMessage("Balance updatedAt is not equal to expected")
        }
        return this
    }

    companion object {
        fun assertBalance(actual: Balance?): BalanceAssert {
            return BalanceAssert(actual)
        }
    }
}
