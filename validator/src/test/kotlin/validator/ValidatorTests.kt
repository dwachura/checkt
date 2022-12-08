package io.dwsoft.checkt.validator

import io.dwsoft.checkt.core.validationSpec
import io.dwsoft.checkt.testing.AlwaysFailingCheck
import io.dwsoft.checkt.testing.alwaysFailWithMessage
import io.dwsoft.checkt.testing.shouldFailBecause
import io.dwsoft.checkt.testing.shouldRepresentCompletedValidation
import io.dwsoft.checkt.testing.violated
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldNotBeNull

// TODO: clean-up
class ValidatorTests : FreeSpec({
    "test1" {
        data class CreateUserRequest(
            val firstName: String = "",
            val lastName: String = "",
            val age: Int = 1,
            val middleName: String? = null,
            val phoneNumber: String? = null,
        ) : Validated<CreateUserRequest> by delegatingTo(
            validationSpec<CreateUserRequest> {
                subject::firstName require { +alwaysFailWithMessage { "1" } }
                subject::lastName require { +alwaysFailWithMessage { "2" } }
                subject::age require { +alwaysFailWithMessage { "3" } }
                subject::middleName require { +alwaysFailWithMessage { "4" } }
                subject::phoneNumber require { +alwaysFailWithMessage { "5" } }
            }.asValidator()
        )

        val request = CreateUserRequest()

        request.asSelfValidated()?.validate()
            .shouldNotBeNull()
            .shouldRepresentCompletedValidation()
            .shouldFailBecause(
                request.firstName.violated<AlwaysFailingCheck>(underPath = { -"" / "firstName" }, withMessage = "1"),
                request.lastName.violated<AlwaysFailingCheck>(underPath = { -"" / "lastName" }, withMessage = "2"),
                request.age.violated<AlwaysFailingCheck>(underPath = { -"" / "age" }, withMessage = "3"),
                request.middleName.violated<AlwaysFailingCheck>(underPath = { -"" / "middleName" }, withMessage = "4"),
                request.phoneNumber.violated<AlwaysFailingCheck>(underPath = { -"" / "phoneNumber" }, withMessage = "5"),
            )
    }
})
