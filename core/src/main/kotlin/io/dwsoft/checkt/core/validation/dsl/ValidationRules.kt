package io.dwsoft.checkt.core.validation.dsl

import io.dwsoft.checkt.core.check.Check
import io.dwsoft.checkt.core.check.GreaterThan
import io.dwsoft.checkt.core.check.InRange
import io.dwsoft.checkt.core.check.InvertedCheck
import io.dwsoft.checkt.core.check.NonNull
import io.dwsoft.checkt.core.check.invert
import io.dwsoft.checkt.core.validation.ValidationError
import io.dwsoft.checkt.core.validation.ValidationResult
import io.dwsoft.checkt.core.validation.toDisplayed

context(ValidationScope)
infix fun <V, K : Check.Key, P : Check.Params> V.must(
    rule: ValidationRule<V, K, P>
): ValidationError<V, K, P>? =
    checkValue(this, rule)

//context(ValidationScope)
//infix fun <V> ValidationError<V, NonNull.Key, NonNull.Params>?.and(
//    nonNullChecks: context(ValidationScope) (nonNullValue: V & Any) -> Unit
//): List<ValidationError<V, *, *>> =
//    this?.let { listOf(this) } ?: listOf()


fun <V> notBeNull(
    errorDetailsBuilder: ErrorDetailsBuilder<V, NonNull.Key, NonNull.Params> =
        { "${validationPath()} must not be null".toDisplayed() },
): ValidationRule<V, NonNull.Key, NonNull.Params> =
    ValidationRule.of(NonNull, errorDetailsBuilder)

fun <V> beNull(
    errorDetailsBuilder: ErrorDetailsBuilder<V, InvertedCheck.Key<NonNull.Key>, NonNull.Params> =
        { "${validationPath()} must be null".toDisplayed() },
): ValidationRule<V, InvertedCheck.Key<NonNull.Key>, NonNull.Params> =
    notBeNull<V>().invert(errorDetailsBuilder)

fun <V> beInRange(
    range: ClosedRange<V>,
    errorDetailsBuilder: ErrorDetailsBuilder<V, InRange.Key, InRange.Params<V>> =
        { "${validationPath()} must be in range ${validationParams.range}".toDisplayed() },
): ValidationRule<V, InRange.Key, InRange.Params<V>>
where V : Comparable<V>, V : Any =
    ValidationRule.of(InRange(range), errorDetailsBuilder)

fun <V> notBeInRange(
    range: ClosedRange<V>,
    errorDetailsBuilder: ErrorDetailsBuilder<V, InvertedCheck.Key<InRange.Key>, InRange.Params<V>> =
        { "${validationPath()} must not be in range ${validationParams.range}".toDisplayed() },
): ValidationRule<V, InvertedCheck.Key<InRange.Key>, InRange.Params<V>>
where V : Comparable<V>, V : Any =
    beInRange(range).invert(errorDetailsBuilder)

fun <V> beGreaterThan(
    min: V,
    errorDetailsBuilder: ErrorDetailsBuilder<V, GreaterThan.Key, GreaterThan.Params<V>> =
        { "${validationPath()} must be greater than ${validationParams.min}".toDisplayed() },
): ValidationRule<V, GreaterThan.Key, GreaterThan.Params<V>>
where V : Comparable<V>, V : Any =
    ValidationRule.of(GreaterThan(min), errorDetailsBuilder)

fun <V> notBeGreaterThan(
    min: V,
    errorDetailsBuilder: ErrorDetailsBuilder<V, InvertedCheck.Key<GreaterThan.Key>, GreaterThan.Params<V>> =
        { "${validationPath()} must not be greater than ${validationParams.min}".toDisplayed() },
): ValidationRule<V, InvertedCheck.Key<GreaterThan.Key>, GreaterThan.Params<V>>
where V : Comparable<V>, V : Any =
    beGreaterThan(min).invert(errorDetailsBuilder)
