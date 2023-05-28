package io.dwsoft.checkt.core

import io.dwsoft.checkt.testing.AlwaysFailing
import io.dwsoft.checkt.testing.AlwaysPassing
import io.dwsoft.checkt.testing.cases
import io.dwsoft.checkt.testing.forAll
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string

class ViolationTests : FreeSpec({
    "violation can be tested against rule descriptor" {
        forAll(
            cases(
                AlwaysFailing.Rule.violation(),
                AlwaysPassing.Rule.violation(),
            )
        ) {
            val result = ifFailedFor(AlwaysFailing.Rule) { true }

            when (context.descriptor) {
                is AlwaysFailing.Rule -> result shouldBe true
                else -> result shouldBe null
            }
        }
    }

    // TODO: investigate type resolving errors
    //  may be required to refactor Violation or/and ValidationContext type parameters
//    "violation can be tested against rule description" {
//        forAll(cases(AlwaysFailing.Rule, AlwaysPassing.Rule)) {
//            val t: ValidationRule.Descriptor<Any?, out Check<Any?>> = this
//            val context = ValidationContext.create(t, ValidationPath())
//            val violation = Violation(Any(), context, Arb.string().next())
//
//            val result = violation.ifFailedFor(AlwaysFailing.Rule) { true }
//
//            when (this) {
//                is AlwaysFailing.Rule -> result shouldBe true
//                else -> result shouldBe null
//            }
//        }
//    }
})

private fun ValidationRule.Descriptor<Any, *>.violation(): Violation<*, *, *> =
    Violation(Any(), ValidationContext.create(this, ValidationPath()), Arb.string().next())
