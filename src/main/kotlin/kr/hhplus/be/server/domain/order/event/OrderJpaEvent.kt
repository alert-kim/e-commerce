package kr.hhplus.be.server.domain.order.event

import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.order.OrderSnapshot
import kr.hhplus.be.server.domain.order.exception.RequiredOrderEventIdException
import java.time.Instant
import jakarta.persistence.*
import kr.hhplus.be.server.infra.order.OrderSnapshotConverter

@Entity
@Table(name = "order_events")
class OrderJpaEvent(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null,
    val orderId: OrderId,
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20)")
    val type: OrderEventType,
    @Convert(converter = OrderSnapshotConverter::class)
    @Column(columnDefinition = "json")
    val snapshot: OrderSnapshot,
    val createdAt: Instant,
) {
    fun id(): OrderEventId = id?.let { OrderEventId(it) } ?: throw RequiredOrderEventIdException()
}
