package kr.hhplus.be.server.infra.order

import kr.hhplus.be.server.domain.order.event.OrderJpaEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface OrderEventJpaRepository : JpaRepository<OrderJpaEvent, Long> {
    @Query("SELECT e FROM OrderJpaEvent e ORDER BY e.id ASC")
    fun findAllByIdAsc(): List<OrderJpaEvent>
    
    @Query("SELECT e FROM OrderJpaEvent e WHERE e.id > :id ORDER BY e.id ASC")
    fun findAllByIdGreaterThanOrderByIdAsc(id: Long): List<OrderJpaEvent>
}
