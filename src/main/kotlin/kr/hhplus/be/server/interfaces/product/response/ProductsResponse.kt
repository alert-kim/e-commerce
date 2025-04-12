package kr.hhplus.be.server.interfaces.product.response

import kr.hhplus.be.server.interfaces.common.ServerApiResponse

class ProductsResponse(
    val totalCount: Long,
    val page: Int,
    val pageSize: Int,
    val products: List<ProductResponse>
): ServerApiResponse
