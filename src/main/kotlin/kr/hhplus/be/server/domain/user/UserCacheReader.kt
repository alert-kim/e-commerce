package kr.hhplus.be.server.domain.user

import kr.hhplus.be.server.common.cache.CacheNames
import kr.hhplus.be.server.domain.user.repository.UserRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class UserCacheReader(
    private val repository: UserRepository,
) {
    @Cacheable(value = [CacheNames.USER], key = "#id", unless = "#result == null")
    fun getOrNull(id: Long): User? =
        repository.findById(id)
}
