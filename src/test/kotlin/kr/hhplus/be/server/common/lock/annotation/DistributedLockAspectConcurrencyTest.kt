package kr.hhplus.be.server.common.lock.annotation

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import kr.hhplus.be.server.common.lock.LockStrategy
import kr.hhplus.be.server.domain.common.LockAcquisitionFailException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

private const val LEASE_MS = 1_000L
private const val EXECUTE_MS = 300L
private const val EXECUTE_LONG_MS = 1_500L
private const val WAIT_MS = 500L
private const val LONG_WAIT_MS = 3_000L

@SpringBootTest
@ActiveProfiles("lock-test")
class DistributedLockAspectConcurrencyTest {

    @Autowired
    lateinit var lockTestService: LockTestService

    @Nested
    @DisplayName("SIMPLE 락")
    inner class SimpleLock {
        @Test
        @DisplayName("동일한 ID에 대한 동시 요청 시 하나만 성공, 나머지는 LockAcquisitionFailException 발생")
        fun successOnlyOne() {
            val threadCount = 10
            val targetId = 101L
            val executorService = Executors.newFixedThreadPool(threadCount)
            val successCounter = AtomicInteger(0)
            val errorCounter = AtomicInteger(0)
            val lockFailureCounter = AtomicInteger(0)
            val barrier = CyclicBarrier(threadCount)
            val completionLatch = CountDownLatch(threadCount)

            repeat(threadCount) {
                executorService.submit {
                    try {
                        barrier.await()

                        lockTestService.executeWithSimpleLock(targetId)

                        successCounter.incrementAndGet()
                    } catch (e: Exception) {
                        when (e) {
                            is LockAcquisitionFailException -> lockFailureCounter.incrementAndGet()
                            else -> errorCounter.incrementAndGet()
                        }
                    } finally {
                        completionLatch.countDown()
                    }
                }
            }
            completionLatch.await()
            executorService.shutdown()

            assertThat(successCounter.get()).isEqualTo(1)
            assertThat(lockFailureCounter.get()).isEqualTo(threadCount - 1)
            assertThat(errorCounter.get()).isEqualTo(0)
        }

        @Test
        @DisplayName("로직 실행이 락 점유 시간을 초과할 경우, 락 해제 (다른 사용자 락 선점 가능)")
        fun releaseIfExceedLeaseTime() {
            val targetId = 102L
            val firstThread = Thread {
                lockTestService.executeTooLongWithSimpleLock(targetId) // 약 1.5초 소요 예정, least time = 1
            }
            firstThread.start()
            Thread.sleep(LEASE_MS + 100) // lease_time 이후

            shouldNotThrowAny {
                lockTestService.executeWithSimpleLock(targetId) // 동일 락 획득 시도
            }
        }

        @Test
        @DisplayName("동시에 다른 ID 요청 시 모두 성공해야 함")
        fun `동시에 다른 ID 요청 시 모두 성공해야 함`() {
            val threadCount = 3
            val executorService = Executors.newFixedThreadPool(threadCount)
            val successCounter = AtomicInteger(0)
            val lockFailureCounter = AtomicInteger(0)
            val barrier = CyclicBarrier(threadCount)
            val completionLatch = CountDownLatch(threadCount)
            for (i in 0 until threadCount) {
                val uniqueId = i.toLong() + 1000
                executorService.submit {
                    try {
                        barrier.await()
                        lockTestService.executeWithSimpleLock(uniqueId)
                        successCounter.incrementAndGet()
                    } catch (e: Exception) {
                        if (e is LockAcquisitionFailException) lockFailureCounter.incrementAndGet()
                    } finally {
                        completionLatch.countDown()
                    }
                }
            }
            completionLatch.await()
            executorService.shutdown()

            assertThat(successCounter.get()).isEqualTo(threadCount)
            assertThat(lockFailureCounter.get()).isEqualTo(0)
        }
    }

    @Nested
    @DisplayName("SPIN락")
    inner class SpinLock {

        @Test
        @DisplayName("로직 실행 시간이 락 대기 보다 짧으면 모든 요청 성공해야 함")
        fun successIfWaitLong() {
            val threadCount = 5
            val targetId = 103L
            val executorService = Executors.newFixedThreadPool(threadCount)
            val barrier = CyclicBarrier(threadCount)
            val successCounter = AtomicInteger(0)
            val lockFailureCounter = AtomicInteger(0)
            val completionLatch = CountDownLatch(threadCount)

            repeat(threadCount) {
                executorService.submit {
                    try {
                        barrier.await()
                        lockTestService.waitLongWithSpinLock(targetId) // 실행시간 0.3초, 대기시간 3초
                        successCounter.incrementAndGet()
                    } catch (e: Exception) {
                        if (e is LockAcquisitionFailException) lockFailureCounter.incrementAndGet()
                    } finally {
                        completionLatch.countDown()
                    }
                }
            }
            completionLatch.await()
            executorService.shutdown()

            assertThat(successCounter.get()).isEqualTo(threadCount)
            assertThat(lockFailureCounter.get()).isEqualTo(0)
        }

        @Test
        @DisplayName("로직 실행이 락 점유 시간을 초과할 경우, 락 해제 (다른 사용자 락 선점 가능)")
        fun releaseIfExceedLeaseTime() {
            val targetId = 104L
            val firstThread = Thread {
                lockTestService.executeTooLongWithSpinLock(targetId) // 약 1.5초 소요 예정, leasE time = 1.0
            }
            firstThread.start()
            Thread.sleep(LEASE_MS + 100) // lease_time 이후

            shouldNotThrowAny {
                lockTestService.executeWithSpinLock(targetId) // 동일 락 획득 시도,
            }
        }

        @Test
        @DisplayName("락 실행(Max(점유, 실행))시간이 락 대기 시간 보다 길 경우 요청 실패")
        fun failIfWaitShort() {
            val targetId = 105L
            val firstThread = Thread {
                lockTestService.executeTooLongWithSpinLock(targetId) // 약 1.5초 소요 예정, lease time = 1.0
            }
            firstThread.start()
            Thread.sleep(100)

            shouldThrow<LockAcquisitionFailException> {
                lockTestService.executeWithSpinLock(targetId) // 동일 락 획득 시도, 대기시간 0.5초
            }
            firstThread.join()
        }
    }

    @Nested
    @DisplayName("PUB_SUB 락")
    inner class PubSubLock {
        @Test
        @DisplayName("로직 실행 시간이 락 대기 보다 짧으면 모든 요청 성공해야 함")
        fun successIfWaitLong() {
            val threadCount = 5
            val targetId = 106L
            val executorService = Executors.newFixedThreadPool(threadCount)
            val barrier = CyclicBarrier(threadCount)
            val successCounter = AtomicInteger(0)
            val lockFailureCounter = AtomicInteger(0)
            val completionLatch = CountDownLatch(threadCount)

            repeat(threadCount) {
                executorService.submit {
                    try {
                        barrier.await()
                        lockTestService.waitLongWithPubSubLock(targetId) // 실행시간 0.3초, 대기시간 3초
                        successCounter.incrementAndGet()
                    } catch (e: Exception) {
                        if (e is LockAcquisitionFailException) lockFailureCounter.incrementAndGet()
                    } finally {
                        completionLatch.countDown()
                    }
                }
            }
            completionLatch.await()
            executorService.shutdown()

            assertThat(successCounter.get()).isEqualTo(threadCount)
            assertThat(lockFailureCounter.get()).isEqualTo(0)
        }

        @Test
        @DisplayName("로직 실행이 락 점유 시간을 초과할 경우, 락 해제 (다른 사용자 락 선점 가능)")
        fun releaseIfExceedLeaseTime() {
            val targetId = 107L
            val firstThread = Thread {
                lockTestService.executeTooLongWithPubSubLock(targetId) // 약 1.5초 소요 예정, least time = 1.0
            }
            firstThread.start()
            Thread.sleep(LEASE_MS + 100) // lease_time 이후

            shouldNotThrowAny {
                lockTestService.executeWithPubSubLock(targetId) // 동일 락 획득 시도,
            }
        }

        @Test
        @DisplayName("락 실행(Max(점유, 실행))시간이 락 대기 시간 보다 길 경우 요청 실패")
        fun failIfWaitShort() {
            val targetId = 108L
            val firstThread = Thread {
                lockTestService.executeTooLongWithPubSubLock(targetId) // 약 1.5초 소요 예정, lease time = 1.0
            }
            firstThread.start()
            Thread.sleep(100)

            shouldThrow<LockAcquisitionFailException> {
                lockTestService.executeWithPubSubLock(targetId) // 동일 락 획득 시도, 대기시간 0.5초
            }
            firstThread.join()
        }
    }
}

@Profile("lock-test")
@Component
open class LockTestService {

    @DistributedLock(
        keyPrefix = "simple-lock",
        identifier = "#id",
        strategy = LockStrategy.SIMPLE,
        waitTime = 0,
        leaseTime = LEASE_MS,
        timeUnit = TimeUnit.MILLISECONDS
    )
    fun executeWithSimpleLock(id: Long): String {
        Thread.sleep(EXECUTE_MS)
        return "Success for ID: $id"
    }

    @DistributedLock(
        keyPrefix = "simple-lock",
        identifier = "#id",
        strategy = LockStrategy.SIMPLE,
        waitTime = 0,
        leaseTime = LEASE_MS,
        timeUnit = TimeUnit.MILLISECONDS
    )
    fun executeTooLongWithSimpleLock(id: Long): String {
        Thread.sleep(LEASE_MS * 3)
        return "Success for ID: $id"
    }


    @DistributedLock(
        keyPrefix = "spin-lock",
        identifier = "#id",
        strategy = LockStrategy.SPIN,
        waitTime = WAIT_MS,
        leaseTime = LEASE_MS,
        timeUnit = TimeUnit.MILLISECONDS
    )
    fun executeWithSpinLock(id: Long): String {
        Thread.sleep(EXECUTE_MS)
        return "Success for ID: $id"
    }

    @DistributedLock(
        keyPrefix = "test-lock-pubsub",
        identifier = "#id",
        strategy = LockStrategy.SPIN,
        waitTime = LONG_WAIT_MS,
        leaseTime = LEASE_MS,
        timeUnit = TimeUnit.MILLISECONDS,
    )
    fun waitLongWithSpinLock(id: Long): String {
        Thread.sleep(EXECUTE_MS)
        return "Success for ID: $id"
    }

    @DistributedLock(
        keyPrefix = "spin-lock",
        identifier = "#id",
        strategy = LockStrategy.SPIN,
        waitTime = LONG_WAIT_MS,
        leaseTime = LEASE_MS,
        timeUnit = TimeUnit.MILLISECONDS
    )
    fun executeTooLongWithSpinLock(id: Long): String {
        Thread.sleep(EXECUTE_LONG_MS)
        return "Success for ID: $id"
    }

    @DistributedLock(
        keyPrefix = "test-lock",
        identifier = "#id",
        strategy = LockStrategy.PUB_SUB,
        waitTime = WAIT_MS,
        leaseTime = LEASE_MS,
        timeUnit = TimeUnit.MILLISECONDS
    )
    fun executeWithPubSubLock(id: Long): String {
        Thread.sleep(EXECUTE_MS)
        return "Success for ID: $id"
    }

    @DistributedLock(
        keyPrefix = "test-lock-pubsub",
        identifier = "#id",
        strategy = LockStrategy.PUB_SUB,
        waitTime = LONG_WAIT_MS,
        leaseTime = LEASE_MS,
        timeUnit = TimeUnit.MILLISECONDS,
    )
    fun waitLongWithPubSubLock(id: Long): String {
        Thread.sleep(EXECUTE_MS)
        return "Success for ID: $id"
    }

    @DistributedLock(
        keyPrefix = "test-lock",
        identifier = "#id",
        strategy = LockStrategy.PUB_SUB,
        waitTime = WAIT_MS,
        leaseTime = LEASE_MS,
        timeUnit = TimeUnit.MILLISECONDS
    )
    fun executeTooLongWithPubSubLock(id: Long): String {
        Thread.sleep(EXECUTE_LONG_MS)
        return "Success for ID: $id"
    }
}


