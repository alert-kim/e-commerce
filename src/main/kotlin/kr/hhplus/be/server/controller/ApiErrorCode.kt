package kr.hhplus.be.server.controller

object BalanceApiErrorCode {
    const val NOT_FOUND_BALANCE_CODE: String = """{"code":"NOT_FOUND_BALANCE"}"""
    const val EXCEED_MAX_BALANCE_CODE: String = """{"code":"EXCEED_MAX_BALANCE"}"""
}

object CouponErrorCode {
    const val ALREADY_USED_COUPON_CODE: String = """{"code":"ALREADY_USED_COUPON"}"""
    const val EXPIRED_COUPON_CODE: String = """{"code":"EXPIRED_COUPON"}"""
    const val NOT_FOUND_COUPON_CODE: String = """{"code":"NOT_FOUND_COUPON"}"""
}

object OrderErrorCode {
    const val EXPIRED_ORDER_CODE: String = """{"code":"EXPIRED_ORDER"}"""
    const val ALREADY_PAID_ORDER_CODE: String = """{"code":"ALREADY_PAID_ORDER"}"""
    const val NOT_FOUND_ORDER_CODE: String = """{"code":"NOT_FOUND_ORDER"}"""
    const val INVALID_ORDER_PRICE_CODE : String = """{"code":"INVALID_ORDER_PRICE"}"""
    const val OUT_OF_PRODUCT_STOCK_CODE : String = """{"code":"OUT_OF_PRODUCT_STOCK"}"""
}


object UserApiErrorCode {
    const val NOT_FOUND_USER_CODE: String = """{"code":"NOT_FOUND_USER"}"""
}


