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
import io.kotest.core.spec.style.scopes.addTest
import io.kotest.matchers.shouldBe
import io.kotest.property.Gen
import kotlin.reflect.KClass

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

fun <T> RootScope.testsFor(cases: Gen<T>, configuration: ChecktTesting<T>.() -> Unit) {
    ChecktTesting<T>().apply(configuration).tests.forEach { (valueExtractor, tests) ->
        tests.forEach { (name, test) ->
            addTest(TestName(name), false, null) {
                forAll(cases) {
                    val value = valueExtractor(this)
                    ChecktTestContext(this, value).test()
                }
            }
        }
    }
}

suspend fun <T> ContainerScope.testsFor(cases: Gen<T>, configuration: ChecktTesting<T>.() -> Unit) {
    ChecktTesting<T>().apply(configuration).tests.forEach { (valueExtractor, tests) ->
        tests.forEach { (name, test) ->
            registerTest(TestName(name), false, null) {
                forAll(cases) {
                    val value = valueExtractor(this)
                    ChecktTestContext(this, value).test()
                }
            }
        }
    }
}

class ChecktTesting<T> {
    private val _tests: MutableList<ChecktTestConfig<T, *>> = mutableListOf()
    internal val tests: List<ChecktTestConfig<T, Any>>
        @Suppress("UNCHECKED_CAST")
        get() = _tests as List<ChecktTestConfig<T, Any>>

    fun <V> fromCase(take: T.() -> V, configuration: ChecktTestScope<T, V>.() -> Unit) {
        _tests += ChecktTestConfig(take, ChecktTestScope<T, V>().apply(configuration).tests)
    }

    fun onCase(configuration: ChecktTestScope<T, T>.() -> Unit) =
        fromCase(take = { this }, configuration)
}

/**
 * Tests for common value type [V]
 */
internal data class ChecktTestConfig<C, V>(
    val valueExtractor: (C) -> V,
    val tests: List<ChecktTest<C, V>>,
)

internal data class ChecktTest<C, V>(
    val name: String,
    val test: suspend ChecktTestContext<C, V>.() -> Unit
)

class ChecktTestScope<C, V> {
    private val _tests: MutableList<ChecktTest<C, V>> = mutableListOf()
    internal val tests: List<ChecktTest<C, V>> by ::_tests

    inline fun <reified T : Check<V>, reified V> check(
        noinline check: TestedCheckFactory<C, V, T>
    ): PartialCheckTestConfig<C, V, T> =
        PartialCheckTestConfig(
            testName = "Check ${T::class.simpleName} taking ${V::class.simpleName} works",
            factory = check
        )

    infix fun <T : Check<V>> PartialCheckTestConfig<C, V, T>.shouldPassWhen(
        predicate: PassWhenPredicate<C, V>
    ): Unit =
        test(testName, factory, predicate)

    private fun test(
        name: String,
        check: ChecktTestContext<C, V>.() -> Check<V>,
        shouldPassWhen: PassWhenPredicate<C, V>,
    ) {
        _tests += ChecktTest(name) {
            check().let { check ->
                when {
                    shouldPassWhen() -> value shouldPass check
                    else -> value shouldNotPass check
                }
            }
        }
    }

    fun <T : Check<*>> rule(rule: TestedRuleFactory<C, V, T>): TestedRuleFactory<C, V, T> =
        rule

    inline infix fun <reified T : Check<*>, reified V> TestedRuleFactory<C, V, T>.shouldPassWhen(
        noinline predicate: PassWhenPredicate<C, V>
    ): PartialRuleTestConfig<C, V, T> =
        PartialRuleTestConfig(
            T::class,
            "Validation rule for ${T::class.simpleName} taking ${V::class.simpleName} works",
            this,
            predicate
        )

    infix fun <T : Check<*>> PartialRuleTestConfig<C, V, T>.orFail(
        expectations: ViolationExpectation<C, V>
    ): Unit =
        test(checkType, testName, factory, predicate, expectations)

    private fun <T : Check<*>> test(
        checkType: KClass<T>,
        name: String,
        rule: ChecktTestContext<C, V>.() -> ValidationRule<V, T>,
        shouldPassWhen: PassWhenPredicate<C, V>,
        with: ViolationExpectation<C, V>,
    ) {
        _tests += ChecktTest(name) {
            testValidation(
                of = value,
                with = validation { +rule() }
            ) {
                when {
                    shouldPassWhen() -> result.shouldBeValid()
                    else -> result.shouldBeInvalidBecause(
                        value.violated(checkType) {
                            ViolationExpectationsContext(case, value, this).with()
                        }
                    )
                }
            }
        }
    }
}

class ChecktTestContext<C, V>(val case: C, val value: V) : ValidationRules<V>

class ViolationExpectationsContext<C, V>(
    val case: C,
    val value: V,
    assertionsDsl: ViolationAssertionsDsl,
) : ViolationAssertionsDsl by assertionsDsl

class PartialCheckTestConfig<CASE, VALUE, CHECK : Check<VALUE>>(
    internal val testName: String,
    internal val factory: TestedCheckFactory<CASE, VALUE, CHECK>,
)

class PartialRuleTestConfig<CASE, VALUE, CHECK : Check<*>>(
    internal val checkType: KClass<CHECK>,
    internal val testName: String,
    internal val factory: TestedRuleFactory<CASE, VALUE, CHECK>,
    internal val predicate: PassWhenPredicate<CASE, VALUE>,
)

private typealias TestedCheckFactory<CASE, VALUE, CHECK> =
        ChecktTestContext<CASE, VALUE>.() -> CHECK

private typealias ViolationExpectation<CASE, VALUE> =
        ViolationExpectationsContext<CASE, VALUE>.() -> Unit

private typealias TestedRuleFactory<CASE, VALUE, CHECK> =
        ChecktTestContext<CASE, VALUE>.() -> ValidationRule<VALUE, CHECK>

private typealias PassWhenPredicate<CASE, VALUE> =
        ChecktTestContext<CASE, VALUE>.() -> Boolean
