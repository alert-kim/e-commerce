package kr.hhplus.be.server.domain.balance

import kr.hhplus.be.server.domain.balance.exception.RequiredBalanceIdException
import kr.hhplus.be.server.domain.balance.result.UsedBalanceAmount
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

class Balance (
    val id: BalanceId? = null,
    val userId: UserId,
    val createdAt: Instant,
    records: List<BalanceRecord>,
    amount: BigDecimal,
    updatedAt: Instant,
) {
    val amount: BigDecimal
        get() = _amount.value

    val records: List<BalanceRecord>
        get() = _records.toList()

    var updatedAt: Instant = updatedAt
        private set

    private var _amount: BalanceAmount = BalanceAmount(amount)
    private val _records: MutableList<BalanceRecord> = records.toMutableList()

    fun charge(amount: BalanceAmount) {
        this._amount = this._amount.plus(amount)
        this.updatedAt = Instant.now()
        addRecord(
            type = BalanceTransactionType.CHARGE,
            amount = amount,
        )
    }

    fun use(amount: BalanceAmount): UsedBalanceAmount  {
        this._amount = this._amount.minus(amount)
        this.updatedAt = Instant.now()
        addRecord(
            type = BalanceTransactionType.USE,
            amount = amount,
        )
        return UsedBalanceAmount(
            balanceId = requireId(),
            amount = amount,
        )
    }

    fun requireId(): BalanceId =
        id ?: throw RequiredBalanceIdException()

    private fun addRecord(
        type: BalanceTransactionType,
        amount: BalanceAmount,
    ) {
        val record = BalanceRecord.new(
            balanceId = requireId(),
            type = type,
            amount = amount,
        )
        _records.add(record)
        this.updatedAt = Instant.now()

    }

    companion object {
        fun new(userId: UserId): Balance =
            Balance(
                userId = userId,
                amount = BigDecimal.ZERO,
                records = emptyList(),
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )
    }
}
