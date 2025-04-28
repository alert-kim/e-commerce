package kr.hhplus.be.server.domain.user.repository

import kr.hhplus.be.server.domain.user.User

interface UserRepository {
    fun findById(id: Long): User?
}
