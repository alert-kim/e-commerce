package kr.hhplus.be.server.application.order.command

data class CreateOrderProcessorCommand(
    val userId: Long,
) {
    companion object {
        fun of(userId: Long): CreateOrderProcessorCommand =
            CreateOrderProcessorCommand(userId)
    }
}