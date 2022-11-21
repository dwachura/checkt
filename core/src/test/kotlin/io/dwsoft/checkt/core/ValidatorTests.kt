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
            ::firstName { this must alwaysFailWithMessage { "1" } }
            ::lastName { this must alwaysFailWithMessage { "2" } }
            ::age { this must alwaysFailWithMessage { "3" } }
            ::middleName { this must alwaysFailWithMessage { "4" } }
            ::phoneNumber { this must alwaysFailWithMessage { "5" } }
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
