package kr.hhplus.be.server.common.lock.annotation

import kr.hhplus.be.server.common.lock.LockStrategy
import java.lang.annotation.Inherited
import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class DistributedLock(
    val keyPrefix: String,
    val identifier: String,
    val waitTime: Long = 1_000,
    val leaseTime: Long = 2_000,
    val timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
    val strategy: LockStrategy = LockStrategy.PUB_SUB
)
