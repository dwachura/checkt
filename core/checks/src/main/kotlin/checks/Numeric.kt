package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules

fun <T : Number> ValidationRules<T>.bePositive(
    errorMessage: LazyErrorMessage<GreaterThan<Double>, T> = { "Number must be positive" }
): ValidationRule<T, GreaterThan<Double>> =
    ValidationRule.create(GreaterThan(0.0), errorMessage) { it.toDouble() }

fun <T : Number> ValidationRules<T>.notBePositive(
    errorMessage: LazyErrorMessage<LessThanOrEqual<Double>, T> = { "Number must not be positive" }
): ValidationRule<T, LessThanOrEqual<Double>> =
    ValidationRule.create(LessThanOrEqual(0.0), errorMessage) { it.toDouble() }

fun <T : Number> ValidationRules<T>.beNegative(
    errorMessage: LazyErrorMessage<LessThan<Double>, T> = { "Number must be negative" }
): ValidationRule<T, LessThan<Double>> =
    ValidationRule.create(LessThan(0.0), errorMessage) { it.toDouble() }

fun <T : Number> ValidationRules<T>.notBeNegative(
    errorMessage: LazyErrorMessage<GreaterThanOrEqual<Double>, T> = { "Number must not be negative" }
): ValidationRule<T, GreaterThanOrEqual<Double>> =
    ValidationRule.create(GreaterThanOrEqual(0.0), errorMessage) { it.toDouble() }
