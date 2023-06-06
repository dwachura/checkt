package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.key

object BePositive : ValidationRule.Descriptor<Number, GreaterThan<Double>>(Check.key())

fun <T : Number> ValidationRules<T>.bePositive(
    errorMessage: LazyErrorMessage<BePositive, T, GreaterThan<Double>> =
        { "Number must be positive" }
): ValidationRule<BePositive, T, GreaterThan<Double>> =
    ValidationRule.create(BePositive, GreaterThan(0.0), errorMessage, Number::toDouble)

object NotBePositive : ValidationRule.Descriptor<Number, LessThanOrEqual<Double>>(Check.key())

fun <T : Number> ValidationRules<T>.notBePositive(
    errorMessage: LazyErrorMessage<NotBePositive, T, LessThanOrEqual<Double>> =
        { "Number must not be positive" }
): ValidationRule<NotBePositive, T, LessThanOrEqual<Double>> =
    ValidationRule.create(NotBePositive, LessThanOrEqual(0.0), errorMessage, Number::toDouble)

object BeNegative : ValidationRule.Descriptor<Number, LessThan<Double>>(Check.key())

fun <T : Number> ValidationRules<T>.beNegative(
    errorMessage: LazyErrorMessage<BeNegative, T, LessThan<Double>> =
        { "Number must be negative" }
): ValidationRule<BeNegative, T, LessThan<Double>> =
    ValidationRule.create(BeNegative, LessThan(0.0), errorMessage, Number::toDouble)

object NotBeNegative : ValidationRule.Descriptor<Number, GreaterThanOrEqual<Double>>(Check.key())

fun <T : Number> ValidationRules<T>.notBeNegative(
    errorMessage: LazyErrorMessage<NotBeNegative, T, GreaterThanOrEqual<Double>> =
        { "Number must not be negative" }
): ValidationRule<NotBeNegative, T, GreaterThanOrEqual<Double>> =
    ValidationRule.create(NotBeNegative, GreaterThanOrEqual(0.0), errorMessage, Number::toDouble)
