package io.dwsoft.checkt.core

import io.dwsoft.checkt.testing.AlwaysFailingCheck
import io.dwsoft.checkt.testing.AlwaysPassingCheck
import io.dwsoft.checkt.testing.cases
import io.dwsoft.checkt.testing.forAll
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string

class ViolationTests : FreeSpec({
    "violation can be tested against check type" {
        forAll(cases(AlwaysFailingCheck, AlwaysPassingCheck)) {
            val context = ValidationContext.create(key, ValidationPath())
            val violation = Violation(Any(), context, Arb.string().next())

            val result = violation.ifFailedFor<AlwaysFailingCheck, _, _> { true }

            when (this) {
                is AlwaysFailingCheck -> result shouldBe true
                else -> result shouldBe null
            }
        }
    }
})
