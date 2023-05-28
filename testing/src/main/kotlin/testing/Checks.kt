package io.dwsoft.checkt.testing

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
import io.dwsoft.checkt.core.Check as CheckBase

suspend infix fun <T> T.shouldPass(check: CheckBase<T>) =
    "Value '$this' should pass check ${check.key.id}".asClue {
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
    "Value '$this' should not pass check ${check.key.id}".asClue {
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

object AlwaysFailing {
    object Check : CheckBase<Any?> by CheckBase({ false })
    object Rule : ValidationRule.Descriptor<Any?, Check>(Check)
}

fun <T> ValidationRules<T>.failWithMessage(
    errorMessage: LazyErrorMessage<AlwaysFailing.Rule, Any?, AlwaysFailing.Check>
): ValidationRule<AlwaysFailing.Rule, T, AlwaysFailing.Check> =
    AlwaysFailing.Check.toValidationRule(AlwaysFailing.Rule, errorMessage)

val <T> ValidationRules<T>.fail: ValidationRule<AlwaysFailing.Rule, T, AlwaysFailing.Check>
    get() = failWithMessage { "$value invalid" }

object AlwaysPassing {
    object Check : CheckBase<Any?> by CheckBase({ true })
    object Rule : ValidationRule.Descriptor<Any?, Check>(Check)
}

val <T> ValidationRules<T>.pass: ValidationRule<AlwaysPassing.Rule, T, AlwaysPassing.Check>
    get() = AlwaysPassing.Check.toValidationRule(AlwaysPassing.Rule) { "" }

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

    inline fun <reified T : CheckBase<V>, reified V> check(
        noinline check: TestedCheckFactory<T, C, V>
    ): PartialCheckTestConfig<T, C, V> =
        PartialCheckTestConfig(
            testName = "Check ${T::class.simpleName} taking ${V::class.simpleName} works",
            factory = check
        )

    infix fun <T : CheckBase<V>> PartialCheckTestConfig<T, C, V>.shouldPassWhen(
        predicate: PassWhenPredicate<C, V>
    ): Unit =
        test(testName, factory, predicate)

    private fun <T : CheckBase<V>> test(
        name: String,
        checkFactory: TestedCheckFactory<T, C, V>,
        shouldPassPredicate: PassWhenPredicate<C, V>,
    ) {
        _tests += ChecktTest(name) {
            checkFactory().let { check ->
                when {
                    shouldPassPredicate() -> value shouldPass check
                    else -> value shouldNotPass check
                }
            }
        }
    }

    fun <D, T> rule(rule: TestedRuleFactory<D, C, V, T>): TestedRuleFactory<D, C, V, T>
            where D : ValidationRule.Descriptor<V, T>, T : CheckBase<*> =
        rule

    inline infix fun <reified D, reified T, reified V> TestedRuleFactory<D, C, V, T>.shouldPassWhen(
        noinline predicate: PassWhenPredicate<C, V>
    ): PartialRuleTestConfig<D, C, V, T>
            where D : ValidationRule.Descriptor<V, T>, T : CheckBase<*> =
        PartialRuleTestConfig(
            "Validation rule for ${T::class.simpleName} taking ${V::class.simpleName} works",
            this,
            predicate
        )

    infix fun <D, T> PartialRuleTestConfig<D, C, V, T>.orFail(
        expectations: ViolationExpectation<C, V>
    ): Unit
            where D : ValidationRule.Descriptor<V, T>, T : CheckBase<*> =
        test(testName, factory, predicate, expectations)

    private fun <D : ValidationRule.Descriptor<V, T>, T : CheckBase<*>> test(
        name: String,
        ruleFactory: TestedRuleFactory<D, C, V, T>,
        shouldPassPredicate: PassWhenPredicate<C, V>,
        violationExpectation: ViolationExpectation<C, V>,
    ) {
        _tests += ChecktTest(name) {
            val rule = ruleFactory()
            testValidation(
                of = value,
                with = validation { +rule }
            ) {
                when {
                    shouldPassPredicate() -> result.shouldBeValid()
                    else -> result.shouldBeInvalidBecause(
                        value.violated(rule.descriptor) {
                            ViolationExpectationsContext(case, value, this).violationExpectation()
                        }
                    )
                }
            }
        }
    }
}

class ChecktTestContext<C, V>(val case: C, val value: V) : ValidationRules<V>

class ViolationExpectationsContext<CASE, VALUE>(
    val case: CASE,
    val value: VALUE,
    assertionsDsl: ViolationAssertionsDsl,
) : ViolationAssertionsDsl by assertionsDsl

class PartialCheckTestConfig<CHECK, CASE, VALUE>(
    internal val testName: String,
    internal val factory: TestedCheckFactory<CHECK, CASE, VALUE>,
) where CHECK : CheckBase<VALUE>

class PartialRuleTestConfig<DESCRIPTOR, CASE, VALUE, CHECK>(
    internal val testName: String,
    internal val factory: TestedRuleFactory<DESCRIPTOR, CASE, VALUE, CHECK>,
    internal val predicate: PassWhenPredicate<CASE, VALUE>,
) where DESCRIPTOR : ValidationRule.Descriptor<VALUE, CHECK>, CHECK : CheckBase<*>

private typealias TestedCheckFactory<CHECK, CASE, VALUE> =
        ChecktTestContext<CASE, VALUE>.() -> CHECK

private typealias ViolationExpectation<CASE, VALUE> =
        ViolationExpectationsContext<CASE, VALUE>.() -> Unit

private typealias TestedRuleFactory<DESCRIPTOR, CASE, VALUE, CHECK> =
        ChecktTestContext<CASE, VALUE>.() -> ValidationRule<DESCRIPTOR, VALUE, CHECK>

private typealias PassWhenPredicate<CASE, VALUE> =
        ChecktTestContext<CASE, VALUE>.() -> Boolean
