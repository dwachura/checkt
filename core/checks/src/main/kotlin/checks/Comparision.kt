package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ParameterizedCheck
import io.dwsoft.checkt.core.ParamsOf
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.and
import io.dwsoft.checkt.core.params
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZonedDateTime

class LessThan<V>(max: V) :
    ParameterizedCheck<Comparable<V>, LessThan.Params<V>> by (
            Params(max) and { it < max }
    )
{
    data class Params<V>(val max: V) : ParamsOf<LessThan<V>, Params<V>>
}

fun <T> ValidationRules<Comparable<T>>.lessThan(
    max: T,
    errorMessage: LazyErrorMessage<LessThan<T>, Comparable<T>> =
        { "Value must be less than ${context.params.max}" },
): ValidationRule<Comparable<T>, LessThan<T>> =
    LessThan(max).toValidationRule(errorMessage)

class LessThanOrEqual<V>(private val max: V) :
    ParameterizedCheck<Comparable<V>, LessThanOrEqual.Params<V>> by (
            Params(max) and { it <= max }
    )
{
    data class Params<V>(val max: V) : ParamsOf<LessThanOrEqual<V>, Params<V>>
}

fun <T> ValidationRules<Comparable<T>>.notGreaterThan(
    max: T,
    errorMessage: LazyErrorMessage<LessThanOrEqual<T>, Comparable<T>> =
        { "Value must not be greater than ${context.params.max}" },
): ValidationRule<Comparable<T>, LessThanOrEqual<T>> =
    LessThanOrEqual(max).toValidationRule(errorMessage)

class GreaterThan<V>(private val min: V) :
    ParameterizedCheck<Comparable<V>, GreaterThan.Params<V>> by (
            Params(min) and { it > min }
    )
{
    data class Params<V>(val min: V) : ParamsOf<GreaterThan<V>, Params<V>>
}

fun <T> ValidationRules<Comparable<T>>.greaterThan(
    min: T,
    errorMessage: LazyErrorMessage<GreaterThan<T>, Comparable<T>> =
        { "Value must be greater than ${context.params.min}" },
): ValidationRule<Comparable<T>, GreaterThan<T>> =
    GreaterThan(min).toValidationRule(errorMessage)

class GreaterThanOrEqual<V>(private val min: V) :
    ParameterizedCheck<Comparable<V>, GreaterThanOrEqual.Params<V>> by (
            Params(min) and { it >= min }
    )
{
    data class Params<V>(val min: V) : ParamsOf<GreaterThanOrEqual<V>, Params<V>>
}

fun <T> ValidationRules<Comparable<T>>.notLessThan(
    min: T,
    errorMessage: LazyErrorMessage<GreaterThanOrEqual<T>, Comparable<T>> =
        { "Value must not be less than ${context.params.min}" },
): ValidationRule<Comparable<T>, GreaterThanOrEqual<T>> =
    GreaterThanOrEqual(min).toValidationRule(errorMessage)

// TODO: extract to other module (additional rules) + test???

fun ValidationRules<Instant>.future(
    now: () -> Instant = Instant::now,
    errorMessage: LazyErrorMessage<GreaterThan<Instant>, Instant> =
        { "Date must represent future" }
): ValidationRule<Instant, GreaterThan<Instant>> =
    GreaterThan(now()).toValidationRule(errorMessage)

fun ValidationRules<LocalDate>.future(
    now: () -> LocalDate = LocalDate::now,
    errorMessage: LazyErrorMessage<GreaterThan<LocalDate>, LocalDate> =
        { "Date must represent future" }
): ValidationRule<LocalDate, GreaterThan<LocalDate>> =
    GreaterThan(now()).toValidationRule(errorMessage)

fun ValidationRules<LocalDateTime>.future(
    now: () -> LocalDateTime = LocalDateTime::now,
    errorMessage: LazyErrorMessage<GreaterThan<LocalDateTime>, LocalDateTime> =
        { "Date must represent future" }
): ValidationRule<LocalDateTime, GreaterThan<LocalDateTime>> =
    GreaterThan(now()).toValidationRule(errorMessage)

fun ValidationRules<OffsetDateTime>.future(
    now: () -> OffsetDateTime = OffsetDateTime::now,
    errorMessage: LazyErrorMessage<GreaterThan<OffsetDateTime>, OffsetDateTime> =
        { "Date must represent future" }
): ValidationRule<OffsetDateTime, GreaterThan<OffsetDateTime>> =
    GreaterThan(now()).toValidationRule(errorMessage)

fun ValidationRules<ZonedDateTime>.future(
    now: () -> ZonedDateTime = ZonedDateTime::now,
    errorMessage: LazyErrorMessage<GreaterThan<ZonedDateTime>, ZonedDateTime> =
        { "Date must represent future" }
): ValidationRule<ZonedDateTime, GreaterThan<ZonedDateTime>> =
    GreaterThan(now()).toValidationRule(errorMessage)

fun ValidationRules<LocalTime>.future(
    now: () -> LocalTime = LocalTime::now,
    errorMessage: LazyErrorMessage<GreaterThan<LocalTime>, LocalTime> =
        { "Time must represent future" }
): ValidationRule<LocalTime, GreaterThan<LocalTime>> =
    GreaterThan(now()).toValidationRule(errorMessage)

fun ValidationRules<OffsetTime>.future(
    now: () -> OffsetTime = OffsetTime::now,
    errorMessage: LazyErrorMessage<GreaterThan<OffsetTime>, OffsetTime> =
        { "Time must represent future" }
): ValidationRule<OffsetTime, GreaterThan<OffsetTime>> =
    GreaterThan(now()).toValidationRule(errorMessage)

fun ValidationRules<Instant>.futureOrPresent(
    now: () -> Instant = Instant::now,
    errorMessage: LazyErrorMessage<GreaterThanOrEqual<Instant>, Instant> =
        { "Date must represent future or present" }
): ValidationRule<Instant, GreaterThanOrEqual<Instant>> =
    GreaterThanOrEqual(now()).toValidationRule(errorMessage)

fun ValidationRules<LocalDate>.futureOrPresent(
    now: () -> LocalDate = LocalDate::now,
    errorMessage: LazyErrorMessage<GreaterThanOrEqual<LocalDate>, LocalDate> =
        { "Date must represent future or present" }
): ValidationRule<LocalDate, GreaterThanOrEqual<LocalDate>> =
    GreaterThanOrEqual(now()).toValidationRule(errorMessage)

fun ValidationRules<LocalDateTime>.futureOrPresent(
    now: () -> LocalDateTime = LocalDateTime::now,
    errorMessage: LazyErrorMessage<GreaterThanOrEqual<LocalDateTime>, LocalDateTime> =
        { "Date must represent future or present" }
): ValidationRule<LocalDateTime, GreaterThanOrEqual<LocalDateTime>> =
    GreaterThanOrEqual(now()).toValidationRule(errorMessage)

fun ValidationRules<OffsetDateTime>.futureOrPresent(
    now: () -> OffsetDateTime = OffsetDateTime::now,
    errorMessage: LazyErrorMessage<GreaterThanOrEqual<OffsetDateTime>, OffsetDateTime> =
        { "Date must represent future or present" }
): ValidationRule<OffsetDateTime, GreaterThanOrEqual<OffsetDateTime>> =
    GreaterThanOrEqual(now()).toValidationRule(errorMessage)

fun ValidationRules<ZonedDateTime>.futureOrPresent(
    now: () -> ZonedDateTime = ZonedDateTime::now,
    errorMessage: LazyErrorMessage<GreaterThanOrEqual<ZonedDateTime>, ZonedDateTime> =
        { "Date must represent future or present" }
): ValidationRule<ZonedDateTime, GreaterThanOrEqual<ZonedDateTime>> =
    GreaterThanOrEqual(now()).toValidationRule(errorMessage)

fun ValidationRules<LocalTime>.futureOrPresent(
    now: () -> LocalTime = LocalTime::now,
    errorMessage: LazyErrorMessage<GreaterThanOrEqual<LocalTime>, LocalTime> =
        { "Time must represent future or present" }
): ValidationRule<LocalTime, GreaterThanOrEqual<LocalTime>> =
    GreaterThanOrEqual(now()).toValidationRule(errorMessage)

fun ValidationRules<OffsetTime>.futureOrPresent(
    now: () -> OffsetTime = OffsetTime::now,
    errorMessage: LazyErrorMessage<GreaterThanOrEqual<OffsetTime>, OffsetTime> =
        { "Time must represent future or present" }
): ValidationRule<OffsetTime, GreaterThanOrEqual<OffsetTime>> =
    GreaterThanOrEqual(now()).toValidationRule(errorMessage)

fun ValidationRules<Instant>.past(
    now: () -> Instant = Instant::now,
    errorMessage: LazyErrorMessage<LessThan<Instant>, Instant> =
        { "Date must represent past" }
): ValidationRule<Instant, LessThan<Instant>> =
    LessThan(now()).toValidationRule(errorMessage)

fun ValidationRules<LocalDate>.past(
    now: () -> LocalDate = LocalDate::now,
    errorMessage: LazyErrorMessage<LessThan<LocalDate>, LocalDate> =
        { "Date must represent past" }
): ValidationRule<LocalDate, LessThan<LocalDate>> =
    LessThan(now()).toValidationRule(errorMessage)

fun ValidationRules<LocalDateTime>.past(
    now: () -> LocalDateTime = LocalDateTime::now,
    errorMessage: LazyErrorMessage<LessThan<LocalDateTime>, LocalDateTime> =
        { "Date must represent past" }
): ValidationRule<LocalDateTime, LessThan<LocalDateTime>> =
    LessThan(now()).toValidationRule(errorMessage)

fun ValidationRules<OffsetDateTime>.past(
    now: () -> OffsetDateTime = OffsetDateTime::now,
    errorMessage: LazyErrorMessage<LessThan<OffsetDateTime>, OffsetDateTime> =
        { "Date must represent past" }
): ValidationRule<OffsetDateTime, LessThan<OffsetDateTime>> =
    LessThan(now()).toValidationRule(errorMessage)

fun ValidationRules<ZonedDateTime>.past(
    now: () -> ZonedDateTime = ZonedDateTime::now,
    errorMessage: LazyErrorMessage<LessThan<ZonedDateTime>, ZonedDateTime> =
        { "Date must represent past" }
): ValidationRule<ZonedDateTime, LessThan<ZonedDateTime>> =
    LessThan(now()).toValidationRule(errorMessage)

fun ValidationRules<LocalTime>.past(
    now: () -> LocalTime = LocalTime::now,
    errorMessage: LazyErrorMessage<LessThan<LocalTime>, LocalTime> =
        { "Time must represent past" }
): ValidationRule<LocalTime, LessThan<LocalTime>> =
    LessThan(now()).toValidationRule(errorMessage)

fun ValidationRules<OffsetTime>.past(
    now: () -> OffsetTime = OffsetTime::now,
    errorMessage: LazyErrorMessage<LessThan<OffsetTime>, OffsetTime> =
        { "Time must represent past" }
): ValidationRule<OffsetTime, LessThan<OffsetTime>> =
    LessThan(now()).toValidationRule(errorMessage)

fun ValidationRules<Instant>.pastOrPresent(
    now: () -> Instant = Instant::now,
    errorMessage: LazyErrorMessage<LessThanOrEqual<Instant>, Instant> =
        { "Date must represent past or present" }
): ValidationRule<Instant, LessThanOrEqual<Instant>> =
    LessThanOrEqual(now()).toValidationRule(errorMessage)

fun ValidationRules<LocalDate>.pastOrPresent(
    now: () -> LocalDate = LocalDate::now,
    errorMessage: LazyErrorMessage<LessThanOrEqual<LocalDate>, LocalDate> =
        { "Date must represent past or present" }
): ValidationRule<LocalDate, LessThanOrEqual<LocalDate>> =
    LessThanOrEqual(now()).toValidationRule(errorMessage)

fun ValidationRules<LocalDateTime>.pastOrPresent(
    now: () -> LocalDateTime = LocalDateTime::now,
    errorMessage: LazyErrorMessage<LessThanOrEqual<LocalDateTime>, LocalDateTime> =
        { "Date must represent past or present" }
): ValidationRule<LocalDateTime, LessThanOrEqual<LocalDateTime>> =
    LessThanOrEqual(now()).toValidationRule(errorMessage)

fun ValidationRules<OffsetDateTime>.pastOrPresent(
    now: () -> OffsetDateTime = OffsetDateTime::now,
    errorMessage: LazyErrorMessage<LessThanOrEqual<OffsetDateTime>, OffsetDateTime> =
        { "Date must represent past or present" }
): ValidationRule<OffsetDateTime, LessThanOrEqual<OffsetDateTime>> =
    LessThanOrEqual(now()).toValidationRule(errorMessage)

fun ValidationRules<ZonedDateTime>.pastOrPresent(
    now: () -> ZonedDateTime = ZonedDateTime::now,
    errorMessage: LazyErrorMessage<LessThanOrEqual<ZonedDateTime>, ZonedDateTime> =
        { "Date must represent past or present" }
): ValidationRule<ZonedDateTime, LessThanOrEqual<ZonedDateTime>> =
    LessThanOrEqual(now()).toValidationRule(errorMessage)

fun ValidationRules<LocalTime>.pastOrPresent(
    now: () -> LocalTime = LocalTime::now,
    errorMessage: LazyErrorMessage<LessThanOrEqual<LocalTime>, LocalTime> =
        { "Time must represent past or present" }
): ValidationRule<LocalTime, LessThanOrEqual<LocalTime>> =
    LessThanOrEqual(now()).toValidationRule(errorMessage)

fun ValidationRules<OffsetTime>.pastOrPresent(
    now: () -> OffsetTime = OffsetTime::now,
    errorMessage: LazyErrorMessage<LessThanOrEqual<OffsetTime>, OffsetTime> =
        { "Time must represent past or present" }
): ValidationRule<OffsetTime, LessThanOrEqual<OffsetTime>> =
    LessThanOrEqual(now()).toValidationRule(errorMessage)
