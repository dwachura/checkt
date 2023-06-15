package io.dwsoft.checkt.testing

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.validation
import io.kotest.core.names.TestName
import io.kotest.core.spec.style.scopes.ContainerScope
import io.kotest.core.spec.style.scopes.RootScope
import io.kotest.core.spec.style.scopes.addTest
import io.kotest.property.Gen

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

    fun <V> taking(asValue: T.() -> V): T.() -> V = asValue

    infix fun <V> (T.() -> V).asValue(configuration: ChecktTestScope<T, V>.() -> Unit) {
        _tests += ChecktTestConfig(this, ChecktTestScope<T, V>().apply(configuration).tests)
    }

    fun takingCaseAsValue(configuration: ChecktTestScope<T, T>.() -> Unit) =
        taking { this } asValue(configuration)
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
        noinline check: TestedCheckFactory<T, C, V>
    ): PartialCheckTestConfig<T, C, V> =
        PartialCheckTestConfig(
            testName = "Check ${T::class.simpleName} taking ${V::class.simpleName} works",
            factory = check
        )

    infix fun <T : Check<V>> PartialCheckTestConfig<T, C, V>.shouldPassWhen(
        predicate: PassWhenPredicate<C, V>
    ): Unit =
        test(testName, factory, predicate)

    private fun <T : Check<V>> test(
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

    fun <D, T> rule(rule: TestedRuleFactory<D, C, V>): TestedRuleFactory<D, C, V>
            where D : ValidationRule.Descriptor<V, T, D>, T : Check<*> =
        rule

    inline infix fun <reified D, reified T, reified V> TestedRuleFactory<D, C, V>.shouldPassWhen(
        noinline predicate: PassWhenPredicate<C, V>
    ): PartialRuleTestConfig<D, C, V, T> where D : ValidationRule.Descriptor<V, T, D>, T : Check<*> =
        PartialRuleTestConfig(
            "Validation rule for ${T::class.simpleName} taking ${V::class.simpleName} works",
            this,
            predicate
        )

    infix fun <D, T> PartialRuleTestConfig<D, C, V, T>.orFail(
        expectations: ViolationExpectation<C, V>
    ): Unit where D : ValidationRule.Descriptor<V, T, D>, T : Check<*> =
        test(testName, factory, predicate, expectations)

    private fun <D : ValidationRule.Descriptor<V, T, D>, T : Check<*>> test(
        name: String,
        ruleFactory: TestedRuleFactory<D, C, V>,
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
) where CHECK : Check<VALUE>

class PartialRuleTestConfig<DESCRIPTOR, CASE, VALUE, CHECK>(
    internal val testName: String,
    internal val factory: TestedRuleFactory<DESCRIPTOR, CASE, VALUE>,
    internal val predicate: PassWhenPredicate<CASE, VALUE>,
) where DESCRIPTOR : ValidationRule.Descriptor<VALUE, CHECK, DESCRIPTOR>, CHECK : Check<*>

private typealias TestedCheckFactory<CHECK, CASE, VALUE> =
        ChecktTestContext<CASE, VALUE>.() -> CHECK

private typealias ViolationExpectation<CASE, VALUE> =
        ViolationExpectationsContext<CASE, VALUE>.() -> Unit

private typealias TestedRuleFactory<DESCRIPTOR, CASE, VALUE> =
        ChecktTestContext<CASE, VALUE>.() -> ValidationRule<DESCRIPTOR, VALUE>

private typealias PassWhenPredicate<CASE, VALUE> =
        ChecktTestContext<CASE, VALUE>.() -> Boolean
