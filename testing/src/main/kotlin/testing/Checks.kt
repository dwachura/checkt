package io.dwsoft.checkt.testing

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ParameterizedCheck
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.key
import io.dwsoft.checkt.core.validation
import io.kotest.assertions.asClue
import io.kotest.core.names.TestName
import io.kotest.core.spec.style.scopes.ContainerScope
import io.kotest.core.spec.style.scopes.RootScope
import io.kotest.core.spec.style.scopes.addContainer
import io.kotest.matchers.shouldBe
import io.kotest.property.Gen

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

suspend inline fun <reified C : Check<V>, T, V> ContainerScope.testCheck(
    runFor: Gen<T>,
    crossinline checking: T.() -> V,
    crossinline validWhen: T.(V) -> Boolean,
    crossinline check: T.() -> C,
) {
    registerTest(TestName("Check works"), false, null) {
        forAll(runFor) {
            val value = checking()
            check().let { check ->
                when {
                    validWhen(value) -> value shouldPass check
                    else -> value shouldNotPass check
                }
            }
        }
    }
}

suspend inline fun <reified C : Check<*>, T, V> ContainerScope.testRule(
    runFor: Gen<T>,
    crossinline checking: T.() -> V,
    crossinline validWhen: T.(V) -> Boolean,
    crossinline rule: ValidationRules<V>.(case: T) -> ValidationRule<V, C>,
    noinline violationMessage: T.(msg: String) -> Unit,
) {
    registerTest(TestName("Rule works"), false, null) {
        forAll(runFor) {
            val value = checking()
            testValidation(
                of = value,
                with = validation { +ValidationRule.rulesFor<V>().rule(this@forAll) }
            ) {
                when {
                    validWhen(value) -> result.shouldBeValid()
                    else -> result.violated<C> { violationMessage(it) }
                }
            }
        }
    }
}

inline fun <reified C : Check<V>, V, T> RootScope.testsFor(
    runFor: Gen<T>,
    crossinline checking: T.() -> V,
    crossinline validWhen: T.(V) -> Boolean,
    crossinline check: T.() -> C,
    crossinline rule: ValidationRules<V>.(case: T) -> ValidationRule<V, C>,
    noinline violationMessage: T.(msg: String) -> Unit,
) {
    addContainer(TestName("${C::class.simpleName}"), false, null) {
        testCheck<C, T, V>(runFor, checking, validWhen, check)
        testRule<C, T, V>(runFor, checking, validWhen, rule, violationMessage)
    }
}
