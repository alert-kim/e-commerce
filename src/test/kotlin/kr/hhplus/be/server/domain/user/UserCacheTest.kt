package kr.hhplus.be.server.domain.user

import kr.hhplus.be.server.common.cache.CacheNames
import kr.hhplus.be.server.domain.user.repository.UserRepository
import kr.hhplus.be.server.testutil.DatabaseTestHelper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
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
class UserCacheTest {

    @Autowired
    private lateinit var cacheReader: UserCacheReader

    @MockitoSpyBean
    private lateinit var repository: UserRepository

    @Autowired
    private lateinit var cacheManager: CacheManager

    @Autowired
    private lateinit var databaseTestHelper: DatabaseTestHelper

    @BeforeEach
    fun setup() {
        cacheManager.getCache(CacheNames.USER)?.clear()
    }

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
