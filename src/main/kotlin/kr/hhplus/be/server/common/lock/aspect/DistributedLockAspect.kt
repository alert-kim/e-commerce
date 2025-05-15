package kr.hhplus.be.server.common.lock.aspect

import kr.hhplus.be.server.common.aop.JoinPointContext
import kr.hhplus.be.server.common.lock.LockStrategy
import kr.hhplus.be.server.common.lock.annotation.DistributedLock
import kr.hhplus.be.server.common.util.spel.SpelEvaluationContextProvider
import kr.hhplus.be.server.domain.common.LockAcquisitionFailException
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.stereotype.Component

@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
class DistributedLockAspect(
    private val redissonClient: RedissonClient,
) {
    @Around("@annotation(kr.hhplus.be.server.common.lock.annotation.DistributedLock)")
    fun doLock(joinPoint: ProceedingJoinPoint): Any? {
        val joinPoint = JoinPointContext(joinPoint)
        val lockKey = generateLockKey(joinPoint)
        return executeWithLock(joinPoint, lockKey)
    }

    private fun getLock(strategy: LockStrategy, key: String): RLock =
        when (strategy) {
            LockStrategy.SIMPLE -> redissonClient.getLock(key)
            LockStrategy.PUB_SUB -> redissonClient.getLock(key)
            LockStrategy.SPIN -> redissonClient.getSpinLock(key)
        }

    private fun generateLockKey(joinPoint: JoinPointContext): String {
        val annotation = joinPoint.getAnnotation<DistributedLock>()
        val context = SpelEvaluationContextProvider.fromJoinPoint(joinPoint)
        val parser = SpelExpressionParser()

        val identifier = parser.parseExpression(annotation.identifier)
            .getValue(context, String::class.java)
            .also { require(!it.isNullOrBlank()) }

        return "${annotation.keyPrefix}:$identifier"
    }

    private fun executeWithLock(joinPoint: JoinPointContext, lockKey: String): Any? {
        val lockAnnotation = joinPoint.getAnnotation<DistributedLock>()

        val lock = getLock(
            lockAnnotation.strategy,
            lockKey,
        )
        val isAcquired = lock.tryLock(
            if (lockAnnotation.strategy == LockStrategy.SIMPLE) 0 else lockAnnotation.waitTime,
            lockAnnotation.leaseTime,
            lockAnnotation.timeUnit,
        )
        if (isAcquired) {
            try {
                return joinPoint.proceed()
            } finally {
                lock.unlock();
            }
        }
        throw LockAcquisitionFailException("for key: $lockKey")
    }
}
