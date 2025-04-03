package kr.hhplus.be.server.controller

object UserApiErrorCode {
    const val NOT_FOUND_USER_CODE: String = """{"code":"NOT_FOUND_USER"}"""
}

object BalanceApiErrorCode {
    const val NOT_FOUND_BALANCE_CODE: String = """{"code":"NOT_FOUND_BALANCE"}"""
    const val EXCEED_MAX_BALANCE_CODE: String = """{"code":"EXCEED_MAX_BALANCE"}"""
}
