package kr.hhplus.be.server.domain.user

import kr.hhplus.be.server.common.cache.CacheNames
import kr.hhplus.be.server.domain.user.exception.NotFoundUserException
import kr.hhplus.be.server.domain.user.repository.UserRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userCacheReader: UserCacheReader,
) {
    @Cacheable(value = [CacheNames.USER], key = "#id")
    fun get(id: Long): UserView {
        val user = userCacheReader.getOrNull(id)
            ?: throw NotFoundUserException("by id: $id")
        return UserView.from(user)
    }
}
