package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules

object BePositive : ValidationRule.Descriptor<Number, GreaterThan<Double>, BePositive> {
    override val defaultMessage: LazyErrorMessage<BePositive, Number> = { "Number must be positive" }
}

val <T : Number> ValidationRules<T>.bePositive: ValidationRule<BePositive, T>
    get() = ValidationRule.create(BePositive, GreaterThan(0.0), Number::toDouble)

object NotBePositive : ValidationRule.Descriptor<Number, LessThanOrEqual<Double>, NotBePositive> {
    override val defaultMessage: LazyErrorMessage<NotBePositive, Number> = { "Number must not be positive" }
}

val <T : Number> ValidationRules<T>.notBePositive: ValidationRule<NotBePositive, T>
    get() = ValidationRule.create(NotBePositive, LessThanOrEqual(0.0), Number::toDouble)

object BeNegative : ValidationRule.Descriptor<Number, LessThan<Double>, BeNegative> {
    override val defaultMessage: LazyErrorMessage<BeNegative, Number> = { "Number must be negative" }
}

val <T : Number> ValidationRules<T>.beNegative: ValidationRule<BeNegative, T>
    get() = ValidationRule.create(BeNegative, LessThan(0.0), Number::toDouble)

object NotBeNegative : ValidationRule.Descriptor<Number, GreaterThanOrEqual<Double>, NotBeNegative> {
    override val defaultMessage: LazyErrorMessage<NotBeNegative, Number> = { "Number must not be negative" }
}

val <T : Number> ValidationRules<T>.notBeNegative: ValidationRule<NotBeNegative, T>
    get() = ValidationRule.create(NotBeNegative, GreaterThanOrEqual(0.0), Number::toDouble)
