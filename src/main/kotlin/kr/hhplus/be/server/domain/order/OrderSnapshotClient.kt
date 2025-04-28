package kr.hhplus.be.server.domain.order

interface OrderSnapshotClient {
    fun send(snapshot: OrderSnapshot)
}
