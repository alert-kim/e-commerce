package kr.hhplus.be.server.common.lock.annotation

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.common.lock.LockStrategy
import kr.hhplus.be.server.common.lock.aspect.DistributedLockAspect
import kr.hhplus.be.server.domain.common.LockAcquisitionFailException
import kr.hhplus.be.server.testutil.mock.UserMock
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import java.lang.reflect.Method
import java.util.concurrent.TimeUnit

class DistributedLockAspectTest {
    private lateinit var redissonClient: RedissonClient
    private lateinit var aspect: DistributedLockAspect
    private lateinit var joinPoint: ProceedingJoinPoint
    private lateinit var methodSignature: MethodSignature
    private lateinit var method: Method
    private lateinit var lock: RLock
    private lateinit var annotation: DistributedLock

    @BeforeEach
    fun setUp() {
        redissonClient = mockk(relaxed = true)
        aspect = DistributedLockAspect(redissonClient)
        joinPoint = mockk(relaxed = true)
        methodSignature = mockk(relaxed = true)
        method = mockk<Method>(relaxed = true)
        lock = mockk(relaxed = true)
        every { joinPoint.signature } returns methodSignature
        every { methodSignature.method } returns method
        every { redissonClient.getLock(any<String>()) } returns lock
        every { redissonClient.getSpinLock(any<String>()) } returns lock
        every { lock.tryLock(any(), any(), any()) } returns true

        val parameter = UserMock.view()
        val annotationIdentifier = "#user.id"
        annotation = mockk<DistributedLock>(relaxed = true) {
            every { keyPrefix } returns "lockKey"
            every { identifier } returns annotationIdentifier
        }
        every { method.getAnnotation(DistributedLock::class.java) } returns annotation
        every { methodSignature.parameterNames } returns arrayOf("user")
        every { joinPoint.args } returns arrayOf(parameter)
    }

    @Test
    @DisplayName("락 획득 성공시 메서드 실행 결과를 반환하고, 락을 해제한다")
    fun acquireLockSuccess() {
        val lock = mockk<RLock>(relaxed = true)
        val expectedResult = "test result"
        every { redissonClient.getLock(any<String>()) } returns lock
        every { lock.tryLock(any(), any(), any()) } returns true
        every { joinPoint.proceed() } returns expectedResult

        val result = aspect.doLock(joinPoint)

        assertThat(result).isEqualTo(expectedResult)
        verify(exactly = 1) { lock.unlock() }
    }

    @Test
    @DisplayName("메서드 실행 중 에러가 발생하면, 해당 에러를 던지고, 락을 해제한다")
    fun executeWithError() {
        val ex = IllegalStateException("test exception")
        every { joinPoint.proceed() } throws ex

        shouldThrow<IllegalStateException> {
            aspect.doLock(joinPoint)
        }

        verify(exactly = 1) { lock.unlock() }
    }

    @Test
    @DisplayName("락 획득 실패시 LockAcquisitionFailException 에러가 발생한다")
    fun acquireLockFail() {
        every { lock.tryLock(any(), any(), any()) } returns false

        shouldThrow<LockAcquisitionFailException> {
            aspect.doLock(joinPoint)
        }
    }

    @Test
    @DisplayName("PUB_SUB을 사용할 경우, 애노테이션의 keyPrefix, identifier에 대한 락을 redissonClient에게 요청한다")
    fun requestPubSubLock() {
        val userParameter = UserMock.view()
        val lockKey = "lockKey"
        val lockKeyIdentifier = "#user.id"
        val lockKeyIdentifierValue = userParameter.id.value
        every { annotation.keyPrefix } returns lockKey
        every { annotation.identifier } returns lockKeyIdentifier
        every { annotation.strategy } returns LockStrategy.PUB_SUB
        every { methodSignature.parameterNames } returns arrayOf("user")
        every { joinPoint.args } returns arrayOf(userParameter)

        aspect.doLock(joinPoint)

        verify(exactly = 1) {
            redissonClient.getLock("$lockKey:$lockKeyIdentifierValue")
        }
    }

    @Test
    @DisplayName("SIMPLE을 사용할 경우, 애노테이션의 keyPrefix, identifier에 대한 락을 redissonClient에게 요청한다")
    fun requestSimpleLock() {
        val userParameter = UserMock.view()
        val lockKey = "lockKey"
        val lockKeyIdentifier = "#user.id"
        val lockKeyIdentifierValue = userParameter.id.value
        every { annotation.keyPrefix } returns lockKey
        every { annotation.identifier } returns lockKeyIdentifier
        every { annotation.strategy } returns LockStrategy.SIMPLE
        every { methodSignature.parameterNames } returns arrayOf("user")
        every { joinPoint.args } returns arrayOf(userParameter)

        aspect.doLock(joinPoint)

        verify(exactly = 1) {
            redissonClient.getLock("$lockKey:$lockKeyIdentifierValue")
        }
    }

    @Test
    @DisplayName("SPIN을 사용할 경우, 애노테이션의 keyPrefix, identifier에 대한 '스핀락'을 redissonClient에게 요청한다")
    fun requestSpinLock() {
        val userParameter = UserMock.view()
        val lockKey = "lockKey"
        val lockKeyIdentifier = "#user.id"
        val lockKeyIdentifierValue = userParameter.id.value
        every { annotation.keyPrefix } returns lockKey
        every { annotation.identifier } returns lockKeyIdentifier
        every { annotation.strategy } returns LockStrategy.SPIN
        every { methodSignature.parameterNames } returns arrayOf("user")
        every { joinPoint.args } returns arrayOf(userParameter)

        aspect.doLock(joinPoint)

        verify(exactly = 1) {
            redissonClient.getSpinLock("$lockKey:$lockKeyIdentifierValue")
        }
    }

    @Test
    @DisplayName("락 획득 시도에는 애노테이션의 waitTime, leaseTime, timeUnit을 사용한다")
    fun acquireLockWithAnnotationParameters() {
        val waitTime = 10L
        val leaseTime = 20L
        val timeUnit = TimeUnit.SECONDS
        every { annotation.waitTime } returns waitTime
        every { annotation.leaseTime } returns leaseTime
        every { annotation.timeUnit } returns timeUnit

        aspect.doLock(joinPoint)

        verify(exactly = 1) {
            lock.tryLock(waitTime, leaseTime, timeUnit)
        }
    }

    @Test
    @DisplayName("SIMPLE락 획득 시도에는, 애노테이션의 waitTime의 상관 없이, waitTime을 0으로 시도한다")
    fun acquireSimpleLockWithAnnotationParameters() {
        val waitTime = 10L
        val leaseTime = 20L
        val timeUnit = TimeUnit.SECONDS
        every { annotation.strategy } returns LockStrategy.SIMPLE
        every { annotation.waitTime } returns waitTime
        every { annotation.leaseTime } returns leaseTime
        every { annotation.timeUnit } returns timeUnit

        aspect.doLock(joinPoint)

        verify(exactly = 1) {
            lock.tryLock(0, leaseTime, timeUnit)
        }
    }
}
