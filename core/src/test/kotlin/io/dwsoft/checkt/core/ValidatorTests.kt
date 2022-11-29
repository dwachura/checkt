package io.dwsoft.checkt.core

import io.dwsoft.checkt.testing.AlwaysFailingCheck
import io.dwsoft.checkt.testing.alwaysFailWithMessage
import io.dwsoft.checkt.testing.shouldFailBecause
import io.dwsoft.checkt.testing.violated
import io.kotest.core.spec.style.FreeSpec

// TODO: clean-up
class ValidatorTests : FreeSpec({
    "test1" {
        data class CreateUserRequest(
            val firstName: String = "",
            val lastName: String = "",
            val age: Int = 1,
            val middleName: String? = null,
            val phoneNumber: String? = null,
        ) : Validated<CreateUserRequest> by validationSpec({
            subject::firstName { +alwaysFailWithMessage { "1" } }
            subject::lastName { +alwaysFailWithMessage { "2" } }
            subject::age { +alwaysFailWithMessage { "3" } }
            subject::middleName { +alwaysFailWithMessage { "4" } }
            subject::phoneNumber { +alwaysFailWithMessage { "5" } }
        })

        val request = CreateUserRequest()

        request.validate().shouldFailBecause(
            request.firstName.violated<AlwaysFailingCheck>(underPath = { -"" / "firstName" }, withMessage = "1"),
            request.lastName.violated<AlwaysFailingCheck>(underPath = { -"" / "lastName" }, withMessage = "2"),
            request.age.violated<AlwaysFailingCheck>(underPath = { -"" / "age" }, withMessage = "3"),
            request.middleName.violated<AlwaysFailingCheck>(underPath = { -"" / "middleName" }, withMessage = "4"),
            request.phoneNumber.violated<AlwaysFailingCheck>(underPath = { -"" / "phoneNumber" }, withMessage = "5"),
        )
    }
})
