package io.dwsoft.checkt.testing

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ParameterizedCheck
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.key
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe

suspend infix fun <T> T.shouldPass(check: Check<T>) =
    "Value '$this' should pass check ${check.key.fullIdentifier}".asClue {
        val assertion: (Check.Result) -> Unit =
            { it.passed shouldBe true }
        when (check) {
            is ParameterizedCheck<T, *> -> {
                val result = check(this)
                result.params.asClue { assertion(result) }
            }
            else -> assertion(check(this))
        }
    }

suspend infix fun <T> T.shouldNotPass(check: Check<T>) =
    "Value '$this' should not pass check ${check.key.fullIdentifier}".asClue {
        val assertion: (Check.Result) -> Unit =
            { it.passed shouldBe false }
        when (check) {
            is ParameterizedCheck<T, *> -> {
                val result = check(this)
                result.params.asClue { assertion(result) }
            }
            else -> assertion(check(this))
        }
    }

val <T> ValidationRules<T>.fail
    get() = failWithMessage { "$value invalid" }

fun <T> ValidationRules<T>.failWithMessage(
    errorMessage: LazyErrorMessage<AlwaysFailingCheck, Any?>
): ValidationRule<T, AlwaysFailingCheck> =
    AlwaysFailingCheck.toValidationRule(errorMessage)

object AlwaysFailingCheck : Check<Any?> by Check({ false })

val <T> ValidationRules<T>.pass
    get() = AlwaysPassingCheck.toValidationRule { "" }

object AlwaysPassingCheck : Check<Any?> by Check({ true })
