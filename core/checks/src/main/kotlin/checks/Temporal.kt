package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZonedDateTime

//class Past<V> : ParameterizedCheck<V, Past.Params<V>> by (
//    Params()
//) {
//    data class Params<V>(now) : ParamsOf<Past<V>, Params<V>>
//}

// TODO: extract to other module (additional rules) + test???

@JvmName("futureInstant")
fun ValidationRules<Instant>.future(
    now: () -> Instant = Instant::now,
    errorMessage: LazyErrorMessage<GreaterThan<Instant>, Instant> =
        { "Date must represent future" }
): ValidationRule<Instant, GreaterThan<Instant>> =
    GreaterThan(now()).toValidationRule(errorMessage)

@JvmName("futureLocalDate")
fun ValidationRules<LocalDate>.future(
    now: () -> LocalDate = LocalDate::now,
    errorMessage: LazyErrorMessage<GreaterThan<LocalDate>, LocalDate> =
        { "Date must represent future" }
): ValidationRule<LocalDate, GreaterThan<LocalDate>> =
    GreaterThan(now()).toValidationRule(errorMessage)

@JvmName("futureLocalDateTime")
fun ValidationRules<LocalDateTime>.future(
    now: () -> LocalDateTime = LocalDateTime::now,
    errorMessage: LazyErrorMessage<GreaterThan<LocalDateTime>, LocalDateTime> =
        { "Date must represent future" }
): ValidationRule<LocalDateTime, GreaterThan<LocalDateTime>> =
    GreaterThan(now()).toValidationRule(errorMessage)

@JvmName("futureOffsetDateTime")
fun ValidationRules<OffsetDateTime>.future(
    now: () -> OffsetDateTime = OffsetDateTime::now,
    errorMessage: LazyErrorMessage<GreaterThan<OffsetDateTime>, OffsetDateTime> =
        { "Date must represent future" }
): ValidationRule<OffsetDateTime, GreaterThan<OffsetDateTime>> =
    GreaterThan(now()).toValidationRule(errorMessage)

@JvmName("futureZonedDateTime")
fun ValidationRules<ZonedDateTime>.future(
    now: () -> ZonedDateTime = ZonedDateTime::now,
    errorMessage: LazyErrorMessage<GreaterThan<ZonedDateTime>, ZonedDateTime> =
        { "Date must represent future" }
): ValidationRule<ZonedDateTime, GreaterThan<ZonedDateTime>> =
    GreaterThan(now()).toValidationRule(errorMessage)

@JvmName("futureLocalTime")
fun ValidationRules<LocalTime>.future(
    now: () -> LocalTime = LocalTime::now,
    errorMessage: LazyErrorMessage<GreaterThan<LocalTime>, LocalTime> =
        { "Time must represent future" }
): ValidationRule<LocalTime, GreaterThan<LocalTime>> =
    GreaterThan(now()).toValidationRule(errorMessage)

@JvmName("futureOffsetTime")
fun ValidationRules<OffsetTime>.future(
    now: () -> OffsetTime = OffsetTime::now,
    errorMessage: LazyErrorMessage<GreaterThan<OffsetTime>, OffsetTime> =
        { "Time must represent future" }
): ValidationRule<OffsetTime, GreaterThan<OffsetTime>> =
    GreaterThan(now()).toValidationRule(errorMessage)

@JvmName("futureOrPresentInstant")
fun ValidationRules<Instant>.futureOrPresent(
    now: () -> Instant = Instant::now,
    errorMessage: LazyErrorMessage<GreaterThanOrEqual<Instant>, Instant> =
        { "Date must represent future or present" }
): ValidationRule<Instant, GreaterThanOrEqual<Instant>> =
    GreaterThanOrEqual(now()).toValidationRule(errorMessage)

@JvmName("futureOrPresentLocalDate")
fun ValidationRules<LocalDate>.futureOrPresent(
    now: () -> LocalDate = LocalDate::now,
    errorMessage: LazyErrorMessage<GreaterThanOrEqual<LocalDate>, LocalDate> =
        { "Date must represent future or present" }
): ValidationRule<LocalDate, GreaterThanOrEqual<LocalDate>> =
    GreaterThanOrEqual(now()).toValidationRule(errorMessage)

@JvmName("futureOrPresentLocalDateTime")
fun ValidationRules<LocalDateTime>.futureOrPresent(
    now: () -> LocalDateTime = LocalDateTime::now,
    errorMessage: LazyErrorMessage<GreaterThanOrEqual<LocalDateTime>, LocalDateTime> =
        { "Date must represent future or present" }
): ValidationRule<LocalDateTime, GreaterThanOrEqual<LocalDateTime>> =
    GreaterThanOrEqual(now()).toValidationRule(errorMessage)

@JvmName("futureOrPresentOffsetDateTime")
fun ValidationRules<OffsetDateTime>.futureOrPresent(
    now: () -> OffsetDateTime = OffsetDateTime::now,
    errorMessage: LazyErrorMessage<GreaterThanOrEqual<OffsetDateTime>, OffsetDateTime> =
        { "Date must represent future or present" }
): ValidationRule<OffsetDateTime, GreaterThanOrEqual<OffsetDateTime>> =
    GreaterThanOrEqual(now()).toValidationRule(errorMessage)

@JvmName("futureOrPresentZonedDateTime")
fun ValidationRules<ZonedDateTime>.futureOrPresent(
    now: () -> ZonedDateTime = ZonedDateTime::now,
    errorMessage: LazyErrorMessage<GreaterThanOrEqual<ZonedDateTime>, ZonedDateTime> =
        { "Date must represent future or present" }
): ValidationRule<ZonedDateTime, GreaterThanOrEqual<ZonedDateTime>> =
    GreaterThanOrEqual(now()).toValidationRule(errorMessage)

@JvmName("futureOrPresentLocalTime")
fun ValidationRules<LocalTime>.futureOrPresent(
    now: () -> LocalTime = LocalTime::now,
    errorMessage: LazyErrorMessage<GreaterThanOrEqual<LocalTime>, LocalTime> =
        { "Time must represent future or present" }
): ValidationRule<LocalTime, GreaterThanOrEqual<LocalTime>> =
    GreaterThanOrEqual(now()).toValidationRule(errorMessage)

@JvmName("futureOrPresentOffsetTime")
fun ValidationRules<OffsetTime>.futureOrPresent(
    now: () -> OffsetTime = OffsetTime::now,
    errorMessage: LazyErrorMessage<GreaterThanOrEqual<OffsetTime>, OffsetTime> =
        { "Time must represent future or present" }
): ValidationRule<OffsetTime, GreaterThanOrEqual<OffsetTime>> =
    GreaterThanOrEqual(now()).toValidationRule(errorMessage)

@JvmName("pastInstant")
fun ValidationRules<Instant>.past(
    now: () -> Instant = Instant::now,
    errorMessage: LazyErrorMessage<LessThan<Instant>, Instant> =
        { "Date must represent past" }
): ValidationRule<Instant, LessThan<Instant>> =
    LessThan(now()).toValidationRule(errorMessage)

@JvmName("pastLocalDate")
fun ValidationRules<LocalDate>.past(
    now: () -> LocalDate = LocalDate::now,
    errorMessage: LazyErrorMessage<LessThan<LocalDate>, LocalDate> =
        { "Date must represent past" }
): ValidationRule<LocalDate, LessThan<LocalDate>> =
    LessThan(now()).toValidationRule(errorMessage)

@JvmName("pastLocalDateTime")
fun ValidationRules<LocalDateTime>.past(
    now: () -> LocalDateTime = LocalDateTime::now,
    errorMessage: LazyErrorMessage<LessThan<LocalDateTime>, LocalDateTime> =
        { "Date must represent past" }
): ValidationRule<LocalDateTime, LessThan<LocalDateTime>> =
    LessThan(now()).toValidationRule(errorMessage)

@JvmName("pastOffsetDateTime")
fun ValidationRules<OffsetDateTime>.past(
    now: () -> OffsetDateTime = OffsetDateTime::now,
    errorMessage: LazyErrorMessage<LessThan<OffsetDateTime>, OffsetDateTime> =
        { "Date must represent past" }
): ValidationRule<OffsetDateTime, LessThan<OffsetDateTime>> =
    LessThan(now()).toValidationRule(errorMessage)

@JvmName("pastZonedDateTime")
fun ValidationRules<ZonedDateTime>.past(
    now: () -> ZonedDateTime = ZonedDateTime::now,
    errorMessage: LazyErrorMessage<LessThan<ZonedDateTime>, ZonedDateTime> =
        { "Date must represent past" }
): ValidationRule<ZonedDateTime, LessThan<ZonedDateTime>> =
    LessThan(now()).toValidationRule(errorMessage)

@JvmName("pastLocalTime")
fun ValidationRules<LocalTime>.past(
    now: () -> LocalTime = LocalTime::now,
    errorMessage: LazyErrorMessage<LessThan<LocalTime>, LocalTime> =
        { "Time must represent past" }
): ValidationRule<LocalTime, LessThan<LocalTime>> =
    LessThan(now()).toValidationRule(errorMessage)

@JvmName("pastOffsetTime")
fun ValidationRules<OffsetTime>.past(
    now: () -> OffsetTime = OffsetTime::now,
    errorMessage: LazyErrorMessage<LessThan<OffsetTime>, OffsetTime> =
        { "Time must represent past" }
): ValidationRule<OffsetTime, LessThan<OffsetTime>> =
    LessThan(now()).toValidationRule(errorMessage)

@JvmName("pastOrPresentInstant")
fun ValidationRules<Instant>.pastOrPresent(
    now: () -> Instant = Instant::now,
    errorMessage: LazyErrorMessage<LessThanOrEqual<Instant>, Instant> =
        { "Date must represent past or present" }
): ValidationRule<Instant, LessThanOrEqual<Instant>> =
    LessThanOrEqual(now()).toValidationRule(errorMessage)

@JvmName("pastOrPresentLocalDate")
fun ValidationRules<LocalDate>.pastOrPresent(
    now: () -> LocalDate = LocalDate::now,
    errorMessage: LazyErrorMessage<LessThanOrEqual<LocalDate>, LocalDate> =
        { "Date must represent past or present" }
): ValidationRule<LocalDate, LessThanOrEqual<LocalDate>> =
    LessThanOrEqual(now()).toValidationRule(errorMessage)

@JvmName("pastOrPresentLocalDateTime")
fun ValidationRules<LocalDateTime>.pastOrPresent(
    now: () -> LocalDateTime = LocalDateTime::now,
    errorMessage: LazyErrorMessage<LessThanOrEqual<LocalDateTime>, LocalDateTime> =
        { "Date must represent past or present" }
): ValidationRule<LocalDateTime, LessThanOrEqual<LocalDateTime>> =
    LessThanOrEqual(now()).toValidationRule(errorMessage)

@JvmName("pastOrPresentOffsetDateTime")
fun ValidationRules<OffsetDateTime>.pastOrPresent(
    now: () -> OffsetDateTime = OffsetDateTime::now,
    errorMessage: LazyErrorMessage<LessThanOrEqual<OffsetDateTime>, OffsetDateTime> =
        { "Date must represent past or present" }
): ValidationRule<OffsetDateTime, LessThanOrEqual<OffsetDateTime>> =
    LessThanOrEqual(now()).toValidationRule(errorMessage)

@JvmName("pastOrPresentZonedDateTime")
fun ValidationRules<ZonedDateTime>.pastOrPresent(
    now: () -> ZonedDateTime = ZonedDateTime::now,
    errorMessage: LazyErrorMessage<LessThanOrEqual<ZonedDateTime>, ZonedDateTime> =
        { "Date must represent past or present" }
): ValidationRule<ZonedDateTime, LessThanOrEqual<ZonedDateTime>> =
    LessThanOrEqual(now()).toValidationRule(errorMessage)

@JvmName("pastOrPresentLocalTime")
fun ValidationRules<LocalTime>.pastOrPresent(
    now: () -> LocalTime = LocalTime::now,
    errorMessage: LazyErrorMessage<LessThanOrEqual<LocalTime>, LocalTime> =
        { "Time must represent past or present" }
): ValidationRule<LocalTime, LessThanOrEqual<LocalTime>> =
    LessThanOrEqual(now()).toValidationRule(errorMessage)

@JvmName("pastOrPresentOffsetTime")
fun ValidationRules<OffsetTime>.pastOrPresent(
    now: () -> OffsetTime = OffsetTime::now,
    errorMessage: LazyErrorMessage<LessThanOrEqual<OffsetTime>, OffsetTime> =
        { "Time must represent past or present" }
): ValidationRule<OffsetTime, LessThanOrEqual<OffsetTime>> =
    LessThanOrEqual(now()).toValidationRule(errorMessage)
