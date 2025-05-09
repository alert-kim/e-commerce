package kr.hhplus.be.server.common.event.aspect

import kr.hhplus.be.server.common.aop.JoinPointContext
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class TransactionalEventListenerAspect {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Around("@annotation(org.springframework.transaction.event.TransactionalEventListener)")
    fun doRunCatchingAndLog(joinPoint: ProceedingJoinPoint): Any? {
        val joinPoint = JoinPointContext(joinPoint)
        val className = joinPoint.className
        val methodName = joinPoint.methodName
        val eventArg = joinPoint.arguments.firstOrNull()

        return runCatching {
            joinPoint.proceed()
        }.onFailure { exception ->
            logger.error("[TransactionEventListener] handle fail: {}#{} - event={} - {}", className, methodName, eventArg, exception.message, exception)
        }.getOrNull()
    }
}
