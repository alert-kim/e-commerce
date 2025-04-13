package kr.hhplus.be.server.domain.product.command

data class AllocateStocksCommand (
    val needStocks: List<NeedStock>,
) {
    data class NeedStock(
        val productId: Long,
        val quantity: Int,
    )
}
