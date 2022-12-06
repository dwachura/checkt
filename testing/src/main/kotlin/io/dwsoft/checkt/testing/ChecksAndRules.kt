package io.dwsoft.checkt.testing

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorDetails
import io.dwsoft.checkt.core.key
import io.dwsoft.checkt.core.toValidationRule
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe

infix fun <T> T.shouldPass(check: Check<T, *, *>) =
    "Value '$this' should pass check ${check.key.fullIdentifier}".asClue {
        "${check.params}".asClue {
            check(this) shouldBe true
        }
    }

infix fun <T> T.shouldNotPass(check: Check<T, *, *>) =
    "Value '$this' should not pass check ${check.key.fullIdentifier}".asClue {
        "${check.params}".asClue {
            check(this) shouldBe false
        }
    }

val alwaysFail = alwaysFailWithMessage { "$value - ${validationPath()}" }

fun alwaysFailWithMessage(
    errorDetails: LazyErrorDetails<AlwaysFailingCheck, Any?, Check.Params.None<AlwaysFailingCheck>>
) = AlwaysFailingCheck.toValidationRule(errorDetails)

object AlwaysFailingCheck : Check.Parameterless<Any?, AlwaysFailingCheck> by Check.Parameterless.delegate(
    implementation = { false }
)

val alwaysPass = AlwaysPassingCheck.toValidationRule { "" }

object AlwaysPassingCheck : Check.Parameterless<Any, AlwaysPassingCheck> by Check.Parameterless.delegate(
    implementation = { true }
)
