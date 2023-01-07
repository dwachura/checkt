package io.dwsoft.checkt.testing

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.key
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe

suspend infix fun <T> T.shouldPass(check: Check<T, *, *>) =
    "Value '$this' should pass check ${check.key.fullIdentifier}".asClue {
        "${check.params}".asClue {
            check(this) shouldBe true
        }
    }

suspend infix fun <T> T.shouldNotPass(check: Check<T, *, *>) =
    "Value '$this' should not pass check ${check.key.fullIdentifier}".asClue {
        "${check.params}".asClue {
            check(this) shouldBe false
        }
    }

val <T> ValidationRules<T>.fail
    get() = failWithMessage { "$value invalid" }

fun <T> ValidationRules<T>.failWithMessage(
    errorMessage: LazyErrorMessage<AlwaysFailingCheck, Any?, Check.Params.None<AlwaysFailingCheck>>
) = AlwaysFailingCheck.toValidationRule(errorMessage)

object AlwaysFailingCheck :
    Check.Parameterless<Any?, AlwaysFailingCheck> by Check.Parameterless.delegate(
        implementation = { false }
    )

val <T> ValidationRules<T>.pass
    get() = AlwaysPassingCheck.toValidationRule { "" }

object AlwaysPassingCheck :
    Check.Parameterless<Any?, AlwaysPassingCheck> by Check.Parameterless.delegate(
        implementation = { true }
    )
