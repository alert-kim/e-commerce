package kr.hhplus.be.server

import jakarta.annotation.PreDestroy
import org.springframework.context.annotation.Configuration
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName

@Configuration
class TestcontainersConfiguration {
    @PreDestroy
    fun preDestroy() {
        if (mySqlContainer.isRunning) mySqlContainer.stop()
        if (redisContainer.isRunning) redisContainer.stop()
    }

    companion object {
        val mySqlContainer: MySQLContainer<*> = MySQLContainer(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("kr/hhplus")
            .withUsername("test")
            .withPassword("test")
            .apply {
                start()
            }

        val redisContainer: GenericContainer<*> = GenericContainer(DockerImageName.parse("redis:latest"))
            .withExposedPorts(6379)
            .apply {
                start()
            }


        init {
            System.setProperty("spring.datasource.url", mySqlContainer.getJdbcUrl() + "?characterEncoding=UTF-8&serverTimezone=UTC")
            System.setProperty("spring.datasource.username", mySqlContainer.username)
            System.setProperty("spring.datasource.password", mySqlContainer.password)

            System.setProperty("spring.data.redis.host", redisContainer.host)
            System.setProperty("spring.data.redis.port", redisContainer.getMappedPort(6379).toString())
        }
    }
}
