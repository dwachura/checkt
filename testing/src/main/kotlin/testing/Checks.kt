package io.dwsoft.checkt.testing

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ParameterizedCheck
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.key
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe

suspend infix fun <T> T.shouldPass(check: Check<T>) =
    "Value '$this' should pass check ${check.key.fullIdentifier}".asClue {
        val assertion = suspend { check(this) shouldBe true }
        (check as? ParameterizedCheck<*, *>)?.params?.asClue {
            assertion()
        } ?: assertion()
    }

suspend infix fun <T> T.shouldNotPass(check: Check<T>) =
    "Value '$this' should not pass check ${check.key.fullIdentifier}".asClue {
        val assertion = suspend { check(this) shouldBe false }
        (check as? ParameterizedCheck<*, *>)?.params?.asClue {
            assertion()
        } ?: assertion()
    }

val <T> ValidationRules<T>.fail
    get() = failWithMessage { "$value invalid" }

fun <T> ValidationRules<T>.failWithMessage(
    errorMessage: LazyErrorMessage<AlwaysFailingCheck, Any?>
) = AlwaysFailingCheck.toValidationRule(errorMessage)

object AlwaysFailingCheck : Check<Any?> by Check.delegate({ false })

val <T> ValidationRules<T>.pass
    get() = AlwaysPassingCheck.toValidationRule { "" }

object AlwaysPassingCheck : Check<Any?>  by Check.delegate({ true })
