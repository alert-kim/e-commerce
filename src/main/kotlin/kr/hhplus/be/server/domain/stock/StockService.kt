package kr.hhplus.be.server.domain.stock

import kr.hhplus.be.server.domain.stock.command.AllocateStocksCommand
import kr.hhplus.be.server.domain.stock.result.AllocatedStockResult
import org.springframework.stereotype.Service

@Service
class StockService() {

    fun allocate(command: AllocateStocksCommand): AllocatedStockResult {
        TODO()
    }
}
