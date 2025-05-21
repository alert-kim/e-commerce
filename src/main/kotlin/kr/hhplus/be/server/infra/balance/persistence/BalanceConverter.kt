package kr.hhplus.be.server.infra.balance.persistence

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import kr.hhplus.be.server.domain.balance.BalanceAmount
import java.math.BigDecimal

@Converter(autoApply = true)
class BalanceAmountConverter : AttributeConverter<BalanceAmount, BigDecimal> {
    override fun convertToDatabaseColumn(amount: BalanceAmount): BigDecimal =
        amount.value

    override fun convertToEntityAttribute(amount: BigDecimal): BalanceAmount =
        BalanceAmount.of(amount)
}
