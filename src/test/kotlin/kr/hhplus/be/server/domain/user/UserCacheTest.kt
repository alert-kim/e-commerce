package kr.hhplus.be.server.domain.user

import kr.hhplus.be.server.common.cache.CacheNames
import kr.hhplus.be.server.domain.user.repository.UserRepository
import kr.hhplus.be.server.testutil.DatabaseTestHelper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean

@SpringBootTest
@Isolated
class UserCacheTest @Autowired constructor(
    private val cacheReader: UserCacheReader,
    private val cacheManager: CacheManager,
    private val databaseTestHelper: DatabaseTestHelper
) {
    @MockitoSpyBean
    private lateinit var repository: UserRepository

    @BeforeEach
    fun setup() {
        cacheManager.getCache(CacheNames.USER)?.clear()
    }

    @Nested
    @DisplayName("캐시 저장")
    inner class Save {

        @Test
        @DisplayName("유저 조회 결과를 캐시에 저장")
        fun saveCache() {
            val user = databaseTestHelper.savedUser()
            val userId = user.id().value

            cacheReader.getOrNull(userId)
            cacheReader.getOrNull(userId)

            Mockito.verify(
                repository,
                times(1)
            ).findById(userId)
        }
    }
}
