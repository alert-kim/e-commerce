package kr.hhplus.be.server.infra.order

import kr.hhplus.be.server.domain.order.event.OrderEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface OrderEventJpaRepository : JpaRepository<OrderEvent, Long> {
    @Query("SELECT e FROM OrderEvent e ORDER BY e.id ASC")
    fun findAllByIdAsc(): List<OrderEvent>
    
    @Query("SELECT e FROM OrderEvent e WHERE e.id > :id ORDER BY e.id ASC")
    fun findAllByIdGreaterThanOrderByIdAsc(id: Long): List<OrderEvent>
}
