package kr.hhplus.be.server.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import kr.hhplus.be.server.common.cache.CacheSpec
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration


@Configuration
@EnableCaching
class CacheConfig {
    private val DEFAULT_TTL: Duration = Duration.ofMinutes(10)

    @Bean
    fun cacheManager(connectionFactory: RedisConnectionFactory): RedisCacheManager {
        val defaultConfig = defaultConfiguration()
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations(defaultConfig))
            .build()
    }

    private fun defaultConfiguration(): RedisCacheConfiguration = RedisCacheConfiguration
        .defaultCacheConfig()
        .entryTtl(DEFAULT_TTL)
        .serializeKeysWith(
            RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
        )
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(GenericJackson2JsonRedisSerializer(objectMapper()))
        )
        .disableCachingNullValues()

    private fun cacheConfigurations(
        config: RedisCacheConfiguration,
    ): Map<String, RedisCacheConfiguration> = CacheSpec.entries
        .associate {
            it.cacheName to config.entryTtl(it.ttl)
        }

    private fun objectMapper(): ObjectMapper = ObjectMapper().apply {
        registerModule(kotlinModule())
        registerModule(JavaTimeModule())
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        activateDefaultTyping(
            BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Any::class.java)
                .build(),
            ObjectMapper.DefaultTyping.EVERYTHING,
        )
    }

    private fun kotlinModule(): KotlinModule =
        KotlinModule.Builder()
            .configure(KotlinFeature.NullToEmptyCollection, true)
            .build()
}
