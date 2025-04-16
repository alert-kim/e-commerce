package kr.hhplus.be.server.interfaces

object BalanceApiErrorCode {
    const val EXCEED_MAX_BALANCE_CODE: String = """{"code":"EXCEED_MAX_BALANCE"}"""
    const val BELOW_MIN_BALANCE_CODE: String = """{"code":"BELOW_MIN_BALANCE"}"""
    const val INSUFFICIENT_BALANCE_CODE: String = """{"code":"INSUFFICIENT_BALANCE"}"""
}

object CouponErrorCode {
    const val ALREADY_HAS_COUPON_CODE: String = """{"code":"ALREADY_HAS_COUPON"}"""
    const val ALREADY_USED_COUPON_CODE: String = """{"code":"ALREADY_USED_COUPON"}"""
    const val EXPIRED_COUPON_CODE: String = """{"code":"EXPIRED_COUPON"}"""
    const val INVALID_STATE_COUPON_CODE: String = """{"code":"INVALID_STATE_COUPON"}"""
    const val NOT_FOUND_COUPON_CODE: String = """{"code":"NOT_FOUND_COUPON"}"""
    const val NOT_OWNED_COUPON_CODE: String = """{"code":"NOT_OWNED_COUPON"}"""
    const val OUT_OF_STOCK_COUPON_CODE: String = """{"code":"OUT_OF_STOCK_COUPON"}"""
}

object OrderErrorCode {
    const val EXPIRED_ORDER_CODE: String = """{"code":"EXPIRED_ORDER"}"""
    const val ALREADY_PAID_ORDER_CODE: String = """{"code":"ALREADY_PAID_ORDER"}"""
    const val NOT_FOUND_ORDER_CODE: String = """{"code":"NOT_FOUND_ORDER"}"""
    const val INVALID_ORDER_PRICE_CODE : String = """{"code":"INVALID_ORDER_PRICE"}"""
}

object ProductErrorCode {
    const val NOT_FOUND_PRODUCT_CODE: String = """{"code":"NOT_FOUND_PRODUCT"}"""
    const val OUT_OF_PRODUCT_STOCK_CODE : String = """{"code":"OUT_OF_PRODUCT_STOCK"}"""
}

object UserApiErrorCode {
    const val NOT_FOUND_USER: String = "NOT_FOUND_USER"
    const val NOT_FOUND_USER_CODE: String = """{"code":${NOT_FOUND_USER}}"""
}

enum class ErrorCode(val message: String) {
    INVALID_REQUEST("잘못된 요청입니다."),

    // balance
    EXCEED_MAX_BALANCE("잔고가 최대치를 초과했습니다."),
    BELOW_MIN_BALANCE("잔고가 최소치 미만입니다"),
    INSUFFICIENT_BALANCE("잔고가 부족합니다."),

    // coupon
    NOT_FOUND_COUPON("쿠폰을 찾을 수 없습니다."),
    ALREADY_USED_COUPON("이미 사용된 쿠폰입니다."),
    EXPIRED_COUPON("쿠폰 사용 기간이 만료되었습니다."),
    NOT_OWNED_COUPON("쿠폰을 소유자가 아닙니다."),

    // order
    INVALID_ORDER_PRICE("잘못된 주문 금액입니다."),

    // product
    NOT_FOUND_PRODUCT("상품을 찾을 수 없습니다."),
    OUT_OF_STOCK_PRODUCT("상품의 재고가 부족합니다."),

    // user
    NOT_FOUND_USER("유저를 찾을 수 없습니다."),

    INTERNAL_SERVER_ERROR("서버 내부 오류입니다."),
}

