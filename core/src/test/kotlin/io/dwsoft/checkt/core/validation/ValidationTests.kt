package io.dwsoft.checkt.core.validation

import io.dwsoft.checkt.core.validation.dsl.beGreaterThan
import io.dwsoft.checkt.core.validation.dsl.must
import io.dwsoft.checkt.core.validation.dsl.notBeNull
import io.dwsoft.checkt.core.validation.dsl.validate
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.types.shouldBeInstanceOf

class ValidationTests : StringSpec({
    "test" {
        data class CreateUserRequest(
            val name: String?,
            val age: Int?,
            val address: String?,
            val foo: Any?,
        )

        val result = validate(
            CreateUserRequest(null, 12, "a", Any()),
            namedAs = "request".toDisplayed(),
        ) {
            validate(::name) {
                this must notBeNull()
            }

            validate(age, namedAs = "age".toDisplayed()) {
                this must notBeNull()
                this!! must beGreaterThan(18)
            }

            address!!.length must beGreaterThan(5) {
                "address's length must be greater than ${validationParams.min}".toDisplayed()
            }
        }

        println(result)
        result.shouldBeInstanceOf<ValidationResult.Failure>()
            .errors.map { it.errorDetails.value } shouldContainAll listOf(
            "request.name must not be null",
            "request.age must be greater than 18",
            "address's length must be greater than 5"
        )
    }
})
