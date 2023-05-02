package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ParameterizedCheck
import io.dwsoft.checkt.core.ParamsOf
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.params
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

// TODO: tests

class Past<V : Temporal<*>>(private val present: () -> V) : ParameterizedCheck<V, Past.Params<V>> {
    override suspend fun invoke(value: V): ParameterizedCheck.Result<Params<V>> =
        present().let {
            ParameterizedCheck.Result(compareValues(value, it) < 0, Params(it))
        }

    data class Params<V : Temporal<*>>(val present: V) : ParamsOf<Past<V>, Params<V>>
}

@JvmName("pastInstant")
fun ValidationRules<Instant>.past(
    now: () -> Instant = Instant::now,
    errorMessage: LazyErrorMessage<Past<Temporal<Instant>>, Instant> =
        { "Date must represent past from ${context.params.present.iso8601}" }
): ValidationRule<Instant, Past<Temporal<Instant>>> =
    ValidationRule.create(Past { Temporal.of(now()) }, errorMessage) { Temporal.of(it) }

@JvmName("pastLocalDate")
fun ValidationRules<LocalDate>.past(
    now: () -> LocalDate = LocalDate::now,
    errorMessage: LazyErrorMessage<Past<Temporal<LocalDate>>, LocalDate> =
        { "Date must represent past from ${context.params.present.iso8601}" }
): ValidationRule<LocalDate, Past<Temporal<LocalDate>>> =
    ValidationRule.create(Past { Temporal.of(now()) }, errorMessage) { Temporal.of(it) }

@JvmName("pastLocalDateTime")
fun ValidationRules<LocalDateTime>.past(
    now: () -> LocalDateTime = LocalDateTime::now,
    errorMessage: LazyErrorMessage<Past<Temporal<LocalDateTime>>, LocalDateTime> =
        { "Date must represent past from ${context.params.present.iso8601}" }
): ValidationRule<LocalDateTime, Past<Temporal<LocalDateTime>>> =
    ValidationRule.create(Past { Temporal.of(now()) }, errorMessage) { Temporal.of(it) }

@JvmName("pastOffsetDateTime")
fun ValidationRules<OffsetDateTime>.past(
    now: () -> OffsetDateTime = OffsetDateTime::now,
    errorMessage: LazyErrorMessage<Past<Temporal<OffsetDateTime>>, OffsetDateTime> =
        { "Date must represent past from ${context.params.present.iso8601}" }
): ValidationRule<OffsetDateTime, Past<Temporal<OffsetDateTime>>> =
    ValidationRule.create(Past { Temporal.of(now()) }, errorMessage) { Temporal.of(it) }

@JvmName("pastZonedDateTime")
fun ValidationRules<ZonedDateTime>.past(
    now: () -> ZonedDateTime = ZonedDateTime::now,
    errorMessage: LazyErrorMessage<Past<Temporal<ZonedDateTime>>, ZonedDateTime> =
        { "Date must represent past from ${context.params.present.iso8601}" }
): ValidationRule<ZonedDateTime, Past<Temporal<ZonedDateTime>>> =
    ValidationRule.create(Past { Temporal.of(now()) }, errorMessage) { Temporal.of(it) }

@JvmName("pastLocalTime")
fun ValidationRules<LocalTime>.past(
    now: () -> LocalTime = LocalTime::now,
    errorMessage: LazyErrorMessage<Past<Temporal<LocalTime>>, LocalTime> =
        { "Date must represent past from ${context.params.present.iso8601}" }
): ValidationRule<LocalTime, Past<Temporal<LocalTime>>> =
    ValidationRule.create(Past { Temporal.of(now()) }, errorMessage) { Temporal.of(it) }

@JvmName("pastOffsetTime")
fun ValidationRules<OffsetTime>.past(
    now: () -> OffsetTime = OffsetTime::now,
    errorMessage: LazyErrorMessage<Past<Temporal<OffsetTime>>, OffsetTime> =
        { "Date must represent past from ${context.params.present.iso8601}" }
): ValidationRule<OffsetTime, Past<Temporal<OffsetTime>>> =
    ValidationRule.create(Past { Temporal.of(now()) }, errorMessage) { Temporal.of(it) }

class PastOrPresent<V : Temporal<*>>(
    private val present: () -> V
) : ParameterizedCheck<V, PastOrPresent.Params<V>> {
    override suspend fun invoke(value: V): ParameterizedCheck.Result<Params<V>> =
        present().let {
            ParameterizedCheck.Result(compareValues(value, it) <= 0, Params(it))
        }

    data class Params<V : Temporal<*>>(val present: V) : ParamsOf<PastOrPresent<V>, Params<V>>
}

@JvmName("pastOrPresentInstant")
fun ValidationRules<Instant>.pastOrPresent(
    now: () -> Instant = Instant::now,
    errorMessage: LazyErrorMessage<PastOrPresent<Temporal<Instant>>, Instant> =
        { "Date must not represent future from ${context.params.present.iso8601}" }
): ValidationRule<Instant, PastOrPresent<Temporal<Instant>>> =
    ValidationRule.create(PastOrPresent { Temporal.of(now()) }, errorMessage) { Temporal.of(it) }

@JvmName("pastOrPresentLocalDate")
fun ValidationRules<LocalDate>.pastOrPresent(
    now: () -> LocalDate = LocalDate::now,
    errorMessage: LazyErrorMessage<PastOrPresent<Temporal<LocalDate>>, LocalDate> =
        { "Date must not represent future from ${context.params.present.iso8601}" }
): ValidationRule<LocalDate, PastOrPresent<Temporal<LocalDate>>> =
    ValidationRule.create(PastOrPresent { Temporal.of(now()) }, errorMessage) { Temporal.of(it) }

@JvmName("pastOrPresentLocalDateTime")
fun ValidationRules<LocalDateTime>.pastOrPresent(
    now: () -> LocalDateTime = LocalDateTime::now,
    errorMessage: LazyErrorMessage<PastOrPresent<Temporal<LocalDateTime>>, LocalDateTime> =
        { "Date must not represent future from ${context.params.present.iso8601}" }
): ValidationRule<LocalDateTime, PastOrPresent<Temporal<LocalDateTime>>> =
    ValidationRule.create(PastOrPresent { Temporal.of(now()) }, errorMessage) { Temporal.of(it) }

@JvmName("pastOrPresentOffsetDateTime")
fun ValidationRules<OffsetDateTime>.pastOrPresent(
    now: () -> OffsetDateTime = OffsetDateTime::now,
    errorMessage: LazyErrorMessage<PastOrPresent<Temporal<OffsetDateTime>>, OffsetDateTime> =
        { "Date must not represent future from ${context.params.present.iso8601}" }
): ValidationRule<OffsetDateTime, PastOrPresent<Temporal<OffsetDateTime>>> =
    ValidationRule.create(PastOrPresent { Temporal.of(now()) }, errorMessage) { Temporal.of(it) }

@JvmName("pastOrPresentZonedDateTime")
fun ValidationRules<ZonedDateTime>.pastOrPresent(
    now: () -> ZonedDateTime = ZonedDateTime::now,
    errorMessage: LazyErrorMessage<PastOrPresent<Temporal<ZonedDateTime>>, ZonedDateTime> =
        { "Date must not represent future from ${context.params.present.iso8601}" }
): ValidationRule<ZonedDateTime, PastOrPresent<Temporal<ZonedDateTime>>> =
    ValidationRule.create(PastOrPresent { Temporal.of(now()) }, errorMessage) { Temporal.of(it) }

@JvmName("pastOrPresentLocalTime")
fun ValidationRules<LocalTime>.pastOrPresent(
    now: () -> LocalTime = LocalTime::now,
    errorMessage: LazyErrorMessage<PastOrPresent<Temporal<LocalTime>>, LocalTime> =
        { "Date must not represent future from ${context.params.present.iso8601}" }
): ValidationRule<LocalTime, PastOrPresent<Temporal<LocalTime>>> =
    ValidationRule.create(PastOrPresent { Temporal.of(now()) }, errorMessage) { Temporal.of(it) }

@JvmName("pastOrPresentOffsetTime")
fun ValidationRules<OffsetTime>.pastOrPresent(
    now: () -> OffsetTime = OffsetTime::now,
    errorMessage: LazyErrorMessage<PastOrPresent<Temporal<OffsetTime>>, OffsetTime> =
        { "Date must not represent future from ${context.params.present.iso8601}" }
): ValidationRule<OffsetTime, PastOrPresent<Temporal<OffsetTime>>> =
    ValidationRule.create(PastOrPresent { Temporal.of(now()) }, errorMessage) { Temporal.of(it) }

class Future<V : Temporal<*>>(private val present: () -> V) : ParameterizedCheck<V, Future.Params<V>> {
    override suspend fun invoke(value: V): ParameterizedCheck.Result<Params<V>> =
        present().let {
            ParameterizedCheck.Result(compareValues(value, it) > 0, Params(it))
        }

    data class Params<V : Temporal<*>>(val present: V) : ParamsOf<Future<V>, Params<V>>
}

@JvmName("futureInstant")
fun ValidationRules<Instant>.future(
    now: () -> Instant = Instant::now,
    errorMessage: LazyErrorMessage<Future<Temporal<Instant>>, Instant> =
        { "Date must represent future from ${context.params.present.iso8601}" }
): ValidationRule<Instant, Future<Temporal<Instant>>> =
    ValidationRule.create(Future { Temporal.of(now()) }, errorMessage) { Temporal.of(it) }

@JvmName("futureLocalDate")
fun ValidationRules<LocalDate>.future(
    now: () -> LocalDate = LocalDate::now,
    errorMessage: LazyErrorMessage<Future<Temporal<LocalDate>>, LocalDate> =
        { "Date must represent future from ${context.params.present.iso8601}" }
): ValidationRule<LocalDate, Future<Temporal<LocalDate>>> =
    ValidationRule.create(Future { Temporal.of(now()) }, errorMessage) { Temporal.of(it) }

@JvmName("futureLocalDateTime")
fun ValidationRules<LocalDateTime>.future(
    now: () -> LocalDateTime = LocalDateTime::now,
    errorMessage: LazyErrorMessage<Future<Temporal<LocalDateTime>>, LocalDateTime> =
        { "Date must represent future from ${context.params.present.iso8601}" }
): ValidationRule<LocalDateTime, Future<Temporal<LocalDateTime>>> =
    ValidationRule.create(Future { Temporal.of(now()) }, errorMessage) { Temporal.of(it) }

@JvmName("futureOffsetDateTime")
fun ValidationRules<OffsetDateTime>.future(
    now: () -> OffsetDateTime = OffsetDateTime::now,
    errorMessage: LazyErrorMessage<Future<Temporal<OffsetDateTime>>, OffsetDateTime> =
        { "Date must represent future from ${context.params.present.iso8601}" }
): ValidationRule<OffsetDateTime, Future<Temporal<OffsetDateTime>>> =
    ValidationRule.create(Future { Temporal.of(now()) }, errorMessage) { Temporal.of(it) }

@JvmName("futureZonedDateTime")
fun ValidationRules<ZonedDateTime>.future(
    now: () -> ZonedDateTime = ZonedDateTime::now,
    errorMessage: LazyErrorMessage<Future<Temporal<ZonedDateTime>>, ZonedDateTime> =
        { "Date must represent future from ${context.params.present.iso8601}" }
): ValidationRule<ZonedDateTime, Future<Temporal<ZonedDateTime>>> =
    ValidationRule.create(Future { Temporal.of(now()) }, errorMessage) { Temporal.of(it) }

@JvmName("futureLocalTime")
fun ValidationRules<LocalTime>.future(
    now: () -> LocalTime = LocalTime::now,
    errorMessage: LazyErrorMessage<Future<Temporal<LocalTime>>, LocalTime> =
        { "Date must represent future from ${context.params.present.iso8601}" }
): ValidationRule<LocalTime, Future<Temporal<LocalTime>>> =
    ValidationRule.create(Future { Temporal.of(now()) }, errorMessage) { Temporal.of(it) }

@JvmName("futureOffsetTime")
fun ValidationRules<OffsetTime>.future(
    now: () -> OffsetTime = OffsetTime::now,
    errorMessage: LazyErrorMessage<Future<Temporal<OffsetTime>>, OffsetTime> =
        { "Date must represent future from ${context.params.present.iso8601}" }
): ValidationRule<OffsetTime, Future<Temporal<OffsetTime>>> =
    ValidationRule.create(Future { Temporal.of(now()) }, errorMessage) { Temporal.of(it) }

class FutureOrPresent<V : Temporal<*>>(
    private val present: () -> V
) : ParameterizedCheck<V, FutureOrPresent.Params<V>> {
    override suspend fun invoke(value: V): ParameterizedCheck.Result<Params<V>> =
        present().let {
            ParameterizedCheck.Result(compareValues(value, it) >= 0, Params(it))
        }

    data class Params<V : Temporal<*>>(val present: V) : ParamsOf<FutureOrPresent<V>, Params<V>>
}

@JvmName("futureOrPresentInstant")
fun ValidationRules<Instant>.futureOrPresent(
    now: () -> Instant = Instant::now,
    errorMessage: LazyErrorMessage<FutureOrPresent<Temporal<Instant>>, Instant> =
        { "Date must not represent past from ${context.params.present.iso8601}" }
): ValidationRule<Instant, FutureOrPresent<Temporal<Instant>>> =
    ValidationRule.create(FutureOrPresent { Temporal.of(now()) }, errorMessage) { Temporal.of(it) }

@JvmName("futureOrPresentLocalDate")
fun ValidationRules<LocalDate>.futureOrPresent(
    now: () -> LocalDate = LocalDate::now,
    errorMessage: LazyErrorMessage<FutureOrPresent<Temporal<LocalDate>>, LocalDate> =
        { "Date must not represent past from ${context.params.present.iso8601}" }
): ValidationRule<LocalDate, FutureOrPresent<Temporal<LocalDate>>> =
    ValidationRule.create(FutureOrPresent { Temporal.of(now()) }, errorMessage) { Temporal.of(it) }

@JvmName("futureOrPresentLocalDateTime")
fun ValidationRules<LocalDateTime>.futureOrPresent(
    now: () -> LocalDateTime = LocalDateTime::now,
    errorMessage: LazyErrorMessage<FutureOrPresent<Temporal<LocalDateTime>>, LocalDateTime> =
        { "Date must not represent past from ${context.params.present.iso8601}" }
): ValidationRule<LocalDateTime, FutureOrPresent<Temporal<LocalDateTime>>> =
    ValidationRule.create(FutureOrPresent { Temporal.of(now()) }, errorMessage) { Temporal.of(it) }

@JvmName("futureOrPresentOffsetDateTime")
fun ValidationRules<OffsetDateTime>.futureOrPresent(
    now: () -> OffsetDateTime = OffsetDateTime::now,
    errorMessage: LazyErrorMessage<FutureOrPresent<Temporal<OffsetDateTime>>, OffsetDateTime> =
        { "Date must not represent past from ${context.params.present.iso8601}" }
): ValidationRule<OffsetDateTime, FutureOrPresent<Temporal<OffsetDateTime>>> =
    ValidationRule.create(FutureOrPresent { Temporal.of(now()) }, errorMessage) { Temporal.of(it) }

@JvmName("futureOrPresentZonedDateTime")
fun ValidationRules<ZonedDateTime>.futureOrPresent(
    now: () -> ZonedDateTime = ZonedDateTime::now,
    errorMessage: LazyErrorMessage<FutureOrPresent<Temporal<ZonedDateTime>>, ZonedDateTime> =
        { "Date must not represent past from ${context.params.present.iso8601}" }
): ValidationRule<ZonedDateTime, FutureOrPresent<Temporal<ZonedDateTime>>> =
    ValidationRule.create(FutureOrPresent { Temporal.of(now()) }, errorMessage) { Temporal.of(it) }

@JvmName("futureOrPresentLocalTime")
fun ValidationRules<LocalTime>.futureOrPresent(
    now: () -> LocalTime = LocalTime::now,
    errorMessage: LazyErrorMessage<FutureOrPresent<Temporal<LocalTime>>, LocalTime> =
        { "Date must not represent past from ${context.params.present.iso8601}" }
): ValidationRule<LocalTime, FutureOrPresent<Temporal<LocalTime>>> =
    ValidationRule.create(FutureOrPresent { Temporal.of(now()) }, errorMessage) { Temporal.of(it) }

@JvmName("futureOrPresentOffsetTime")
fun ValidationRules<OffsetTime>.futureOrPresent(
    now: () -> OffsetTime = OffsetTime::now,
    errorMessage: LazyErrorMessage<FutureOrPresent<Temporal<OffsetTime>>, OffsetTime> =
        { "Date must not represent past from ${context.params.present.iso8601}" }
): ValidationRule<OffsetTime, FutureOrPresent<Temporal<OffsetTime>>> =
    ValidationRule.create(FutureOrPresent { Temporal.of(now()) }, errorMessage) { Temporal.of(it) }

/**
 * Interface used to group types that represents temporal values, introduced so that unified
 * checks can be defined.
 *
 * New implementation should extend one of the pre-defined sub-interfaces that corresponds to
 * its kind, i.e. [Date], [Time] or [DateTime]. It also should be added in a form of factory
 * function defined as an extension of [the companion object][Temporal.Companion] of this interface
 * (see examples for predefined implementations [Temporal.Companion.of]) together with corresponding
 * [rule][ValidationRule] factory function (e.g. [ValidationRules.past]).
 */
sealed interface Temporal<T : Comparable<T>> : Comparable<Temporal<T>> {
    val value: T
    val iso8601: String

    interface Date<T : Comparable<T>> : Temporal<T>

    interface Time<T : Comparable<T>> : Temporal<T> {
        val isLocal: Boolean
    }

    interface DateTime<T : Comparable<T>> : Temporal<T> {
        val isLocal: Boolean
    }

    override fun compareTo(other: Temporal<T>): Int = value.compareTo(other.value)

    companion object
}

fun Temporal.Companion.of(value: Instant): Temporal<Instant> =
    object : Temporal.DateTime<Instant> {
        override val value: Instant = value
        override val isLocal: Boolean = false
        override val iso8601: String = DateTimeFormatter.ISO_INSTANT.format(this.value)
    }

fun Temporal.Companion.of(value: LocalDateTime): Temporal<LocalDateTime> =
    object : Temporal.DateTime<LocalDateTime> {
        override val value: LocalDateTime = value
        override val isLocal: Boolean = true
        override val iso8601: String = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(this.value)
    }

fun Temporal.Companion.of(value: OffsetDateTime): Temporal<OffsetDateTime> =
    object : Temporal.DateTime<OffsetDateTime> {
        override val value: OffsetDateTime = value
        override val isLocal: Boolean = false
        override val iso8601: String = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this.value)
    }

fun Temporal.Companion.of(value: ZonedDateTime): Temporal<ZonedDateTime> =
    object : Temporal.DateTime<ZonedDateTime> {
        override val value: ZonedDateTime = value
        override val isLocal: Boolean = false
        override val iso8601: String = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this.value)
    }

fun Temporal.Companion.of(value: LocalDate): Temporal<LocalDate> =
    object : Temporal.Date<LocalDate> {
        override val value: LocalDate = value
        override val iso8601: String = DateTimeFormatter.ISO_LOCAL_DATE.format(this.value)
    }

fun Temporal.Companion.of(value: LocalTime): Temporal<LocalTime> =
    object : Temporal.Time<LocalTime> {
        override val value: LocalTime = value
        override val isLocal: Boolean = true
        override val iso8601: String = DateTimeFormatter.ISO_LOCAL_TIME.format(this.value)
    }

fun Temporal.Companion.of(value: OffsetTime): Temporal<OffsetTime> =
    object : Temporal.Time<OffsetTime> {
        override val value: OffsetTime = value
        override val isLocal: Boolean = true
        override val iso8601: String = DateTimeFormatter.ISO_OFFSET_TIME.format(this.value)
    }
