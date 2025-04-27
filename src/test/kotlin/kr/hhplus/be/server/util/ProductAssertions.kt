package kr.hhplus.be.server.util

import kr.hhplus.be.server.domain.product.Product
import org.assertj.core.api.AbstractAssert

class ProductAssert(actual: Product?) : AbstractAssert<ProductAssert, Product>(actual, ProductAssert::class.java) {

    fun isEqualTo(expected: Product): ProductAssert {
        when {
            actual == null -> failWithMessage("Product is null")
            actual.id() != expected.id() -> failWithMessage("product id is not equal to expected")
            actual.name != expected.name -> failWithMessage("product name is not equal to expected")
            actual.description != expected.description -> failWithMessage("product description is not equal to expected")
            actual.price.compareTo(expected.price) != 0 -> failWithMessage("product price is not equal to expected")
            actual.status != expected.status -> failWithMessage("product status is not equal to expected")
            actual.createdAt != expected.createdAt -> failWithMessage("product createdAt is not equal to expected")
            actual.updatedAt != expected.updatedAt -> failWithMessage("product createdAt is not equal to expected")
        }
        return this
    }

    companion object {
        fun assertProduct(actual: Product?): ProductAssert {
            return ProductAssert(actual)
        }
    }
}
