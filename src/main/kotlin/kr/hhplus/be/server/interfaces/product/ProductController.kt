package kr.hhplus.be.server.interfaces.product

import kr.hhplus.be.server.application.product.ProductFacade
import kr.hhplus.be.server.domain.common.InvalidPageRequestArgumentException
import kr.hhplus.be.server.interfaces.ErrorCode
import kr.hhplus.be.server.interfaces.ErrorSpec
import kr.hhplus.be.server.interfaces.common.handleRequest
import kr.hhplus.be.server.interfaces.product.response.ProductsPageResponse
import kr.hhplus.be.server.interfaces.product.response.ProductsListResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductController(
    private val productFacade: ProductFacade,
) : ProductControllerInterface {

    @GetMapping("/products")
    override fun getProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") pageSize: Int,
    ) = handleRequest(
        block = {
            val productsPaged = productFacade.getAllOnSalePaged(page = page, pageSize = pageSize)
            ProductsPageResponse.from(productsPaged)
        },
        errorSpec = {
            when (it) {
                is InvalidPageRequestArgumentException -> ErrorSpec.badRequest(ErrorCode.INVALID_REQUEST)
                else -> ErrorSpec.serverError(ErrorCode.INTERNAL_SERVER_ERROR)
            }
        }
    )

    @GetMapping("/products/popular")
    override fun getPopularProducts() =
        handleRequest(
            block = {
                val result = productFacade.getPopularProducts()
                ProductsListResponse.from(result)
            },
            errorSpec = {
                when (it) {
                    else -> ErrorSpec.serverError(ErrorCode.INTERNAL_SERVER_ERROR)
                }
            }
        )
}
