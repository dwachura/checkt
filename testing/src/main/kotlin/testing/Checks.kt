package io.dwsoft.checkt.testing

import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ParameterizedCheck
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.key
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.dwsoft.checkt.core.Check as CheckBase

suspend infix fun <T> T.shouldPass(check: CheckBase<T>) =
    "Value '$this' should pass check ${check.key}".asClue {
        val assertion: (CheckBase.Result) -> Unit =
            { it.passed shouldBe true }
        when (check) {
            is ParameterizedCheck<T, *> -> {
                val result = check(this)
                result.params.asClue { assertion(result) }
            }

            else -> assertion(check(this))
        }
    }

suspend infix fun <T> T.shouldNotPass(check: CheckBase<T>) =
    "Value '$this' should not pass check ${check.key}".asClue {
        val assertion: (CheckBase.Result) -> Unit =
            { it.passed shouldBe false }
        when (check) {
            is ParameterizedCheck<T, *> -> {
                val result = check(this)
                result.params.asClue { assertion(result) }
            }

            else -> assertion(check(this))
        }
    }

object AlwaysFailing : CheckBase<Any?> by CheckBase({ false }) {
    object Rule : ValidationRule.Descriptor<Any?, AlwaysFailing, Rule> {
        override val defaultMessage: LazyErrorMessage<Rule, Any?> =
            { "$value invalid" }
    }
}

val <T> ValidationRules<T>.fail: ValidationRule<AlwaysFailing.Rule, T>
    get() = AlwaysFailing.toValidationRule(AlwaysFailing.Rule)

object AlwaysPassing : CheckBase<Any?> by CheckBase({ true }) {
    object Rule : ValidationRule.Descriptor<Any?, AlwaysPassing, Rule> {
        override val defaultMessage: LazyErrorMessage<Rule, Any?> = { "" }
    }
}

val <T> ValidationRules<T>.pass: ValidationRule<AlwaysPassing.Rule, T>
    get() = AlwaysPassing.toValidationRule(AlwaysPassing.Rule)
