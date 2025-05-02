package kr.hhplus.be.server.config

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RedissonConfig(

) {
    @Value("\${spring.redis.host}")
    lateinit var redisHost: String
    @Value("\${spring.redis.port}")
    lateinit var redisPort: Integer
    private val REDIS_HOST_PREFIX: String = "redis://"

    @Bean
    fun redissonClient(): RedissonClient {
        val config = Config()

        config.useSingleServer()
            .setAddress("${REDIS_HOST_PREFIX}$redisHost:$redisPort")

        return Redisson.create(config)
    }
}
