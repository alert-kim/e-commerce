package kr.hhplus.be.server.common.lock

enum class LockStrategy {
    SIMPLE,
    SPIN,
    PUB_SUB
}
