package io.dwsoft.checkt.testing

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.ValidationPath
import io.dwsoft.checkt.core.ValidationPathBuilder
import io.dwsoft.checkt.core.ValidationPathBuildingScope
import io.dwsoft.checkt.core.ValidationResult
import io.dwsoft.checkt.core.ValidationResult.Failure
import io.dwsoft.checkt.core.ValidationScope
import io.dwsoft.checkt.core.checkKey
import io.dwsoft.checkt.core.joinToString
import io.dwsoft.checkt.core.key
import io.dwsoft.checkt.core.toList
import io.dwsoft.checkt.core.unnamed
import io.dwsoft.checkt.core.validationPath
import io.kotest.assertions.asClue
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Gen
import io.kotest.property.checkAll

infix fun <V> V.shouldPass(check: Check<V, *, *>) =
    "Value '$this' should pass check ${check.key.fullIdentifier}".asClue {
        "${check.params}".asClue {
            check(this) shouldBe true
        }
    }

infix fun <V> V.shouldNotPass(check: Check<V, *, *>) =
    "Value '$this' should not pass check ${check.key.fullIdentifier}".asClue {
        "${check.params}".asClue {
            check(this) shouldBe false
        }
    }

suspend fun <T> forAll(cases: Gen<T>, verify: T.() -> Unit) {
    checkAll(cases) { verify(it) }
}

fun ValidationPath.shouldContainSegments(expected: List<ValidationPath.Segment>) {
    val actualSegments = toList()
    "Validation path with segments $actualSegments, should contain passed values.".asClue {
        actualSegments.shouldContainInOrder(expected)
    }
}

fun ValidationPath.shouldContainSegments(vararg expected: ValidationPath.Segment) =
    this.shouldContainSegments(expected.toList())

fun ValidationScope.shouldFailBecause(vararg violations: Violation<*>) =
    result.shouldFailBecause(*violations)

fun ValidationResult.shouldFailBecause(vararg violations: Violation<*>) {
    shouldBeInstanceOf<Failure>()
        .errors.also { errors ->
            assertSoftly {
                violations.forEach { violation ->
                    val (expectedValue, expectedPath) = violation
                    val expectedCheckType = violation.check.shortIdentifier
                    val readableExpectedPath = expectedPath.joinToString()
                    val maybeError = errors.firstOrNull {
                        it.validationPath == expectedPath
                                && it.validatedValue == expectedValue
                    }
                    val error =
                        "Violation of $expectedCheckType by value '$expectedValue' on path '$readableExpectedPath' not found"
                            .asClue { maybeError.shouldNotBeNull() }
                    "Asserting error message of $expectedCheckType violation on path '$readableExpectedPath' (value: '$expectedValue')"
                        .asClue { violation.errorMessageAssertions(error.errorDetails) }
                }
            }
        }
}

data class Violation<C : Check<*, *, *>>(
    val value: Any?,
    val path: ValidationPath,
    val check: Check.Key<C>,
    val errorMessageAssertions: (String) -> Unit,
)

inline fun <reified C : Check<*, *, *>> Any?.violated(
    noinline underPath: ValidationPathBuilder = { ValidationPath.unnamed },
    noinline withMessageThat: (String) -> Unit = {},
): Violation<C> =
    Violation(this, validationPath(underPath), C::class.checkKey(), withMessageThat)

inline fun <reified C : Check<*, *, *>> Any?.violated(
    noinline underPath: ValidationPathBuilder = { ValidationPath.unnamed },
    withMessage: String,
): Violation<C> =
    Violation(this, validationPath(underPath), C::class.checkKey()) { it shouldBe withMessage }



