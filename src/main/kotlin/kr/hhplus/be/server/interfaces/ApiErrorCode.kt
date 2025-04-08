package kr.hhplus.be.server.interfaces

object BalanceApiErrorCode {
    const val EXCEED_MAX_BALANCE_CODE: String = """{"code":"EXCEED_MAX_BALANCE"}"""
}

object CouponErrorCode {
    const val ALREADY_HAS_COUPON_CODE: String = """{"code":"ALREADY_HAS_COUPON"}"""
    const val ALREADY_USED_COUPON_CODE: String = """{"code":"ALREADY_USED_COUPON"}"""
    const val EXPIRED_COUPON_CODE: String = """{"code":"EXPIRED_COUPON"}"""
    const val NOT_FOUND_COUPON_CODE: String = """{"code":"NOT_FOUND_COUPON"}"""
    const val OUT_OF_STOCK_COUPON_CODE: String = """{"code":"OUT_OF_STOCK_COUPON"}"""
    const val INVALID_STATE_COUPON_CODE: String = """{"code":"INVALID_STATE_COUPON"}"""
}

object OrderErrorCode {
    const val EXPIRED_ORDER_CODE: String = """{"code":"EXPIRED_ORDER"}"""
    const val ALREADY_PAID_ORDER_CODE: String = """{"code":"ALREADY_PAID_ORDER"}"""
    const val NOT_FOUND_ORDER_CODE: String = """{"code":"NOT_FOUND_ORDER"}"""
    const val INVALID_ORDER_PRICE_CODE : String = """{"code":"INVALID_ORDER_PRICE"}"""
    const val OUT_OF_PRODUCT_STOCK_CODE : String = """{"code":"OUT_OF_PRODUCT_STOCK"}"""
}


object UserApiErrorCode {
    const val NOT_FOUND_USER: String = "NOT_FOUND_USER"
    const val NOT_FOUND_USER_CODE: String = """{"code":${NOT_FOUND_USER}}"""
}

enum class ErrorCode(val message: String) {
    NOT_FOUND_USER("유저를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR("서버 내부 오류입니다."),
}

