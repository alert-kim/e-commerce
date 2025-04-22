package kr.hhplus.be.server.util

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class DatabaseTestHelperConfig {

    @Bean
    fun testHelper(helper: DatabaseTestHelper): DatabaseTestHelper {
        return helper
    }
}

