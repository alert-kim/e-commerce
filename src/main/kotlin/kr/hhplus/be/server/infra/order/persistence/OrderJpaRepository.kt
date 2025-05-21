package kr.hhplus.be.server.infra.order.persistence

import kr.hhplus.be.server.domain.order.Order
import org.springframework.data.jpa.repository.JpaRepository

interface OrderJpaRepository : JpaRepository<Order, Long>
