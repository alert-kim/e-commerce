package kr.hhplus.be.server.controller

object BalanceApiErrorCode {
    const val NOT_FOUND_BALANCE_CODE: String = """{"code":"NOT_FOUND_BALANCE"}"""
    const val EXCEED_MAX_BALANCE_CODE: String = """{"code":"EXCEED_MAX_BALANCE"}"""
}

object OrderErrorCode {
    const val INVALID_ORDER_PRICE_CODE : String = """{"code":"INVALID_ORDER_PRICE"}"""
    const val OUT_OF_PRODUCT_STOCK_CODE : String = """{"code":"OUT_OF_PRODUCT_STOCK"}"""
}


object UserApiErrorCode {
    const val NOT_FOUND_USER_CODE: String = """{"code":"NOT_FOUND_USER"}"""
}


