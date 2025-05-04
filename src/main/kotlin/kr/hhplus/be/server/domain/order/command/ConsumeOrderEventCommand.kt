package kr.hhplus.be.server.domain.order.command

import kr.hhplus.be.server.domain.order.event.OrderJpaEvent

data class ConsumeOrderEventCommand(
    val consumerId: String,
    val event: OrderJpaEvent,
) {
    companion object {
        fun of(
            consumerId: String,
            events: List<OrderJpaEvent>,
        ): ConsumeOrderEventCommand {
            require(events.isNotEmpty()) {
                "이벤트가 존재하지 않습니다."
            }
            require(events.distinctBy { it.type }.size == 1) {
                "이벤트 타입이 같아야 합니다. ${events.map { it.type }}" // 현재는 이벤트 타입이 한 개라 테스트를 진행하지 않음. 타입이 추가되면, 테스트를 추가해야 함.
            }

            return ConsumeOrderEventCommand(
                consumerId = consumerId,
                event = events.maxBy { it.id().value },
            )
        }
    }
}
