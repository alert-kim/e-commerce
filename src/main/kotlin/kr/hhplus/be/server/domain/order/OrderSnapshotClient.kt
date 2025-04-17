package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.order.dto.OrderSnapshot

interface OrderSnapshotClient {
    fun send(snapshot: OrderSnapshot)
}
