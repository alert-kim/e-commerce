package kr.hhplus.be.server.infra.user.persistence

import kr.hhplus.be.server.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository : JpaRepository<User, Long>
