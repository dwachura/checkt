package io.dwsoft.checkt.validator

import io.dwsoft.checkt.core.validation
import io.dwsoft.checkt.testing.failWithMessage
import io.dwsoft.checkt.testing.failed
import io.dwsoft.checkt.testing.shouldBeInvalidBecause
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldNotBeNull

// TODO: clean-up
class ValidatorTests : FreeSpec({
    "simple dto" {
        data class CreateUserRequest(
            val firstName: String = "",
            val lastName: String = "",
            val age: Int = 1,
            val middleName: String? = null,
            val phoneNumber: String? = null,
        ) : Validated<CreateUserRequest> by delegate(validation {
            subject::firstName require { +failWithMessage { "1" } }
            subject::lastName require { +failWithMessage { "2" } }
            subject::age require { +failWithMessage { "3" } }
            subject::middleName require { +failWithMessage { "4" } }
            subject::phoneNumber require { +failWithMessage { "5" } }
        })

        val request = CreateUserRequest()

        request.asSelfValidated()?.validate()
            .shouldNotBeNull()
            .shouldBeInvalidBecause(
                request.firstName.failed(underPath = { -"firstName" }, withMessage = "1"),
                request.lastName.failed(underPath = { -"lastName" }, withMessage = "2"),
                request.age.failed(underPath = { -"age" }, withMessage = "3"),
                request.middleName.failed(underPath = { -"middleName" }, withMessage = "4"),
                request.phoneNumber.failed(underPath = { -"phoneNumber" }, withMessage = "5"),
            )
    }

    "value class" {
        class ValueClass(val value: String) :
            Validated<ValueClass> by delegate(
                validating = { value },
                by = validation { +failWithMessage { "1" } }
            )

        val validated = ValueClass("abcd")

        validated.asSelfValidated()?.validate()
            .shouldNotBeNull()
            .shouldBeInvalidBecause(validated.value.failed(withMessage = "1"))
    }
})
