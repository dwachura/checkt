package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ParameterizedCheck
import io.dwsoft.checkt.core.ParamsOf
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.key
import io.dwsoft.checkt.core.params
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class Past<V : Temporal<*>>(private val present: () -> V) : ParameterizedCheck<V, Past.Params<V>> {
    override suspend fun invoke(value: V): ParameterizedCheck.Result<Params<V>> =
        present().let {
            ParameterizedCheck.Result(compareValues(value, it) < 0, Params(it))
        }

    data class Params<V : Temporal<*>>(val present: V) : ParamsOf<Past<V>, Params<V>>

    class RuleDescriptor<T> : ValidationRule.Descriptor<T, Past<Temporal<T>>>(Check.key())
}

@JvmName("pastInstant")
fun ValidationRules<Instant>.past(
    now: () -> Instant = Instant::now,
    errorMessage: LazyErrorMessage<Past.RuleDescriptor<Instant>, Instant, Past<Temporal<Instant>>> =
        { "Date must represent past from ${context.params.present.iso8601}" }
): ValidationRule<Past.RuleDescriptor<Instant>, Instant, Past<Temporal<Instant>>> =
    ValidationRule.create(Past.RuleDescriptor(), Past { Temporal.from(now()) }, errorMessage) {
        Temporal.from(it)
    }

@JvmName("pastLocalDate")
fun ValidationRules<LocalDate>.past(
    now: () -> LocalDate = LocalDate::now,
    errorMessage: LazyErrorMessage<Past.RuleDescriptor<LocalDate>, LocalDate, Past<Temporal<LocalDate>>> =
        { "Date must represent past from ${context.params.present.iso8601}" }
): ValidationRule<Past.RuleDescriptor<LocalDate>, LocalDate, Past<Temporal<LocalDate>>> =
    ValidationRule.create(Past.RuleDescriptor(), Past { Temporal.from(now()) }, errorMessage) {
        Temporal.from(it)
    }

@JvmName("pastLocalDateTime")
fun ValidationRules<LocalDateTime>.past(
    now: () -> LocalDateTime = LocalDateTime::now,
    errorMessage: LazyErrorMessage<Past.RuleDescriptor<LocalDateTime>, LocalDateTime, Past<Temporal<LocalDateTime>>> =
        { "Date must represent past from ${context.params.present.iso8601}" }
): ValidationRule<Past.RuleDescriptor<LocalDateTime>, LocalDateTime, Past<Temporal<LocalDateTime>>> =
    ValidationRule.create(Past.RuleDescriptor(), Past { Temporal.from(now()) }, errorMessage) {
        Temporal.from(it)
    }

@JvmName("pastOffsetDateTime")
fun ValidationRules<OffsetDateTime>.past(
    now: () -> OffsetDateTime = OffsetDateTime::now,
    errorMessage: LazyErrorMessage<Past.RuleDescriptor<OffsetDateTime>, OffsetDateTime, Past<Temporal<OffsetDateTime>>> =
        { "Date must represent past from ${context.params.present.iso8601}" }
): ValidationRule<Past.RuleDescriptor<OffsetDateTime>, OffsetDateTime, Past<Temporal<OffsetDateTime>>> =
    ValidationRule.create(Past.RuleDescriptor(), Past { Temporal.from(now()) }, errorMessage) {
        Temporal.from(it)
    }

@JvmName("pastZonedDateTime")
fun ValidationRules<ZonedDateTime>.past(
    now: () -> ZonedDateTime = ZonedDateTime::now,
    errorMessage: LazyErrorMessage<Past.RuleDescriptor<ZonedDateTime>, ZonedDateTime, Past<Temporal<ZonedDateTime>>> =
        { "Date must represent past from ${context.params.present.iso8601}" }
): ValidationRule<Past.RuleDescriptor<ZonedDateTime>, ZonedDateTime, Past<Temporal<ZonedDateTime>>> =
    ValidationRule.create(Past.RuleDescriptor(), Past { Temporal.from(now()) }, errorMessage) {
        Temporal.from(it)
    }

@JvmName("pastLocalTime")
fun ValidationRules<LocalTime>.past(
    now: () -> LocalTime = LocalTime::now,
    errorMessage: LazyErrorMessage<Past.RuleDescriptor<LocalTime>, LocalTime, Past<Temporal<LocalTime>>> =
        { "Date must represent past from ${context.params.present.iso8601}" }
): ValidationRule<Past.RuleDescriptor<LocalTime>, LocalTime, Past<Temporal<LocalTime>>> =
    ValidationRule.create(Past.RuleDescriptor(), Past { Temporal.from(now()) }, errorMessage) {
        Temporal.from(it)
    }

@JvmName("pastOffsetTime")
fun ValidationRules<OffsetTime>.past(
    now: () -> OffsetTime = OffsetTime::now,
    errorMessage: LazyErrorMessage<Past.RuleDescriptor<OffsetTime>, OffsetTime, Past<Temporal<OffsetTime>>> =
        { "Date must represent past from ${context.params.present.iso8601}" }
): ValidationRule<Past.RuleDescriptor<OffsetTime>, OffsetTime, Past<Temporal<OffsetTime>>> =
    ValidationRule.create(Past.RuleDescriptor(), Past { Temporal.from(now()) }, errorMessage) {
        Temporal.from(it)
    }

class PastOrPresent<V : Temporal<*>>(
    private val present: () -> V
) : ParameterizedCheck<V, PastOrPresent.Params<V>> {
    override suspend fun invoke(value: V): ParameterizedCheck.Result<Params<V>> =
        present().let {
            ParameterizedCheck.Result(compareValues(value, it) <= 0, Params(it))
        }

    data class Params<V : Temporal<*>>(val present: V) : ParamsOf<PastOrPresent<V>, Params<V>>

    class RuleDescriptor<T> : ValidationRule.Descriptor<T, PastOrPresent<Temporal<T>>>(Check.key())
}

@JvmName("pastOrPresentInstant")
fun ValidationRules<Instant>.pastOrPresent(
    now: () -> Instant = Instant::now,
    errorMessage: LazyErrorMessage<PastOrPresent.RuleDescriptor<Instant>, Instant, PastOrPresent<Temporal<Instant>>> =
        { "Date must not represent future from ${context.params.present.iso8601}" }
): ValidationRule<PastOrPresent.RuleDescriptor<Instant>, Instant, PastOrPresent<Temporal<Instant>>> =
    ValidationRule.create(PastOrPresent.RuleDescriptor(), PastOrPresent { Temporal.from(now()) }, errorMessage) { Temporal.from(it) }

@JvmName("pastOrPresentLocalDate")
fun ValidationRules<LocalDate>.pastOrPresent(
    now: () -> LocalDate = LocalDate::now,
    errorMessage: LazyErrorMessage<PastOrPresent.RuleDescriptor<LocalDate>, LocalDate, PastOrPresent<Temporal<LocalDate>>> =
        { "Date must not represent future from ${context.params.present.iso8601}" }
): ValidationRule<PastOrPresent.RuleDescriptor<LocalDate>, LocalDate, PastOrPresent<Temporal<LocalDate>>> =
    ValidationRule.create(PastOrPresent.RuleDescriptor(), PastOrPresent { Temporal.from(now()) }, errorMessage) { Temporal.from(it) }

@JvmName("pastOrPresentLocalDateTime")
fun ValidationRules<LocalDateTime>.pastOrPresent(
    now: () -> LocalDateTime = LocalDateTime::now,
    errorMessage: LazyErrorMessage<PastOrPresent.RuleDescriptor<LocalDateTime>, LocalDateTime, PastOrPresent<Temporal<LocalDateTime>>> =
        { "Date must not represent future from ${context.params.present.iso8601}" }
): ValidationRule<PastOrPresent.RuleDescriptor<LocalDateTime>, LocalDateTime, PastOrPresent<Temporal<LocalDateTime>>> =
    ValidationRule.create(PastOrPresent.RuleDescriptor(), PastOrPresent { Temporal.from(now()) }, errorMessage) { Temporal.from(it) }

@JvmName("pastOrPresentOffsetDateTime")
fun ValidationRules<OffsetDateTime>.pastOrPresent(
    now: () -> OffsetDateTime = OffsetDateTime::now,
    errorMessage: LazyErrorMessage<PastOrPresent.RuleDescriptor<OffsetDateTime>, OffsetDateTime, PastOrPresent<Temporal<OffsetDateTime>>> =
        { "Date must not represent future from ${context.params.present.iso8601}" }
): ValidationRule<PastOrPresent.RuleDescriptor<OffsetDateTime>, OffsetDateTime, PastOrPresent<Temporal<OffsetDateTime>>> =
    ValidationRule.create(PastOrPresent.RuleDescriptor(), PastOrPresent { Temporal.from(now()) }, errorMessage) { Temporal.from(it) }

@JvmName("pastOrPresentZonedDateTime")
fun ValidationRules<ZonedDateTime>.pastOrPresent(
    now: () -> ZonedDateTime = ZonedDateTime::now,
    errorMessage: LazyErrorMessage<PastOrPresent.RuleDescriptor<ZonedDateTime>, ZonedDateTime, PastOrPresent<Temporal<ZonedDateTime>>> =
        { "Date must not represent future from ${context.params.present.iso8601}" }
): ValidationRule<PastOrPresent.RuleDescriptor<ZonedDateTime>, ZonedDateTime, PastOrPresent<Temporal<ZonedDateTime>>> =
    ValidationRule.create(PastOrPresent.RuleDescriptor(), PastOrPresent { Temporal.from(now()) }, errorMessage) { Temporal.from(it) }

@JvmName("pastOrPresentLocalTime")
fun ValidationRules<LocalTime>.pastOrPresent(
    now: () -> LocalTime = LocalTime::now,
    errorMessage: LazyErrorMessage<PastOrPresent.RuleDescriptor<LocalTime>, LocalTime, PastOrPresent<Temporal<LocalTime>>> =
        { "Date must not represent future from ${context.params.present.iso8601}" }
): ValidationRule<PastOrPresent.RuleDescriptor<LocalTime>, LocalTime, PastOrPresent<Temporal<LocalTime>>> =
    ValidationRule.create(PastOrPresent.RuleDescriptor(), PastOrPresent { Temporal.from(now()) }, errorMessage) { Temporal.from(it) }

@JvmName("pastOrPresentOffsetTime")
fun ValidationRules<OffsetTime>.pastOrPresent(
    now: () -> OffsetTime = OffsetTime::now,
    errorMessage: LazyErrorMessage<PastOrPresent.RuleDescriptor<OffsetTime>, OffsetTime, PastOrPresent<Temporal<OffsetTime>>> =
        { "Date must not represent future from ${context.params.present.iso8601}" }
): ValidationRule<PastOrPresent.RuleDescriptor<OffsetTime>, OffsetTime, PastOrPresent<Temporal<OffsetTime>>> =
    ValidationRule.create(PastOrPresent.RuleDescriptor(), PastOrPresent { Temporal.from(now()) }, errorMessage) { Temporal.from(it) }

class Future<V : Temporal<*>>(private val present: () -> V) : ParameterizedCheck<V, Future.Params<V>> {
    override suspend fun invoke(value: V): ParameterizedCheck.Result<Params<V>> =
        present().let {
            ParameterizedCheck.Result(compareValues(value, it) > 0, Params(it))
        }

    data class Params<V : Temporal<*>>(val present: V) : ParamsOf<Future<V>, Params<V>>

    class RuleDescriptor<T> : ValidationRule.Descriptor<T, Future<Temporal<T>>>(Check.key())
}

@JvmName("futureInstant")
fun ValidationRules<Instant>.future(
    now: () -> Instant = Instant::now,
    errorMessage: LazyErrorMessage<Future.RuleDescriptor<Instant>, Instant, Future<Temporal<Instant>>> =
        { "Date must represent future from ${context.params.present.iso8601}" }
): ValidationRule<Future.RuleDescriptor<Instant>, Instant, Future<Temporal<Instant>>> =
    ValidationRule.create(Future.RuleDescriptor(), Future { Temporal.from(now()) }, errorMessage) { Temporal.from(it) }

@JvmName("futureLocalDate")
fun ValidationRules<LocalDate>.future(
    now: () -> LocalDate = LocalDate::now,
    errorMessage: LazyErrorMessage<Future.RuleDescriptor<LocalDate>, LocalDate, Future<Temporal<LocalDate>>> =
        { "Date must represent future from ${context.params.present.iso8601}" }
): ValidationRule<Future.RuleDescriptor<LocalDate>, LocalDate, Future<Temporal<LocalDate>>> =
    ValidationRule.create(Future.RuleDescriptor(), Future { Temporal.from(now()) }, errorMessage) { Temporal.from(it) }

@JvmName("futureLocalDateTime")
fun ValidationRules<LocalDateTime>.future(
    now: () -> LocalDateTime = LocalDateTime::now,
    errorMessage: LazyErrorMessage<Future.RuleDescriptor<LocalDateTime>, LocalDateTime, Future<Temporal<LocalDateTime>>> =
        { "Date must represent future from ${context.params.present.iso8601}" }
): ValidationRule<Future.RuleDescriptor<LocalDateTime>, LocalDateTime, Future<Temporal<LocalDateTime>>> =
    ValidationRule.create(Future.RuleDescriptor(), Future { Temporal.from(now()) }, errorMessage) { Temporal.from(it) }

@JvmName("futureOffsetDateTime")
fun ValidationRules<OffsetDateTime>.future(
    now: () -> OffsetDateTime = OffsetDateTime::now,
    errorMessage: LazyErrorMessage<Future.RuleDescriptor<OffsetDateTime>, OffsetDateTime, Future<Temporal<OffsetDateTime>>> =
        { "Date must represent future from ${context.params.present.iso8601}" }
): ValidationRule<Future.RuleDescriptor<OffsetDateTime>, OffsetDateTime, Future<Temporal<OffsetDateTime>>> =
    ValidationRule.create(Future.RuleDescriptor(), Future { Temporal.from(now()) }, errorMessage) { Temporal.from(it) }

@JvmName("futureZonedDateTime")
fun ValidationRules<ZonedDateTime>.future(
    now: () -> ZonedDateTime = ZonedDateTime::now,
    errorMessage: LazyErrorMessage<Future.RuleDescriptor<ZonedDateTime>, ZonedDateTime, Future<Temporal<ZonedDateTime>>> =
        { "Date must represent future from ${context.params.present.iso8601}" }
): ValidationRule<Future.RuleDescriptor<ZonedDateTime>, ZonedDateTime, Future<Temporal<ZonedDateTime>>> =
    ValidationRule.create(Future.RuleDescriptor(), Future { Temporal.from(now()) }, errorMessage) { Temporal.from(it) }

@JvmName("futureLocalTime")
fun ValidationRules<LocalTime>.future(
    now: () -> LocalTime = LocalTime::now,
    errorMessage: LazyErrorMessage<Future.RuleDescriptor<LocalTime>, LocalTime, Future<Temporal<LocalTime>>> =
        { "Date must represent future from ${context.params.present.iso8601}" }
): ValidationRule<Future.RuleDescriptor<LocalTime>, LocalTime, Future<Temporal<LocalTime>>> =
    ValidationRule.create(Future.RuleDescriptor(), Future { Temporal.from(now()) }, errorMessage) { Temporal.from(it) }

@JvmName("futureOffsetTime")
fun ValidationRules<OffsetTime>.future(
    now: () -> OffsetTime = OffsetTime::now,
    errorMessage: LazyErrorMessage<Future.RuleDescriptor<OffsetTime>, OffsetTime, Future<Temporal<OffsetTime>>> =
        { "Date must represent future from ${context.params.present.iso8601}" }
): ValidationRule<Future.RuleDescriptor<OffsetTime>, OffsetTime, Future<Temporal<OffsetTime>>> =
    ValidationRule.create(Future.RuleDescriptor(), Future { Temporal.from(now()) }, errorMessage) { Temporal.from(it) }

class FutureOrPresent<V : Temporal<*>>(
    private val present: () -> V
) : ParameterizedCheck<V, FutureOrPresent.Params<V>> {
    override suspend fun invoke(value: V): ParameterizedCheck.Result<Params<V>> =
        present().let {
            ParameterizedCheck.Result(compareValues(value, it) >= 0, Params(it))
        }

    data class Params<V : Temporal<*>>(val present: V) : ParamsOf<FutureOrPresent<V>, Params<V>>

    class RuleDescriptor<T> : ValidationRule.Descriptor<T, FutureOrPresent<Temporal<T>>>(Check.key())
}

@JvmName("futureOrPresentInstant")
fun ValidationRules<Instant>.futureOrPresent(
    now: () -> Instant = Instant::now,
    errorMessage: LazyErrorMessage<FutureOrPresent.RuleDescriptor<Instant>, Instant, FutureOrPresent<Temporal<Instant>>> =
        { "Date must not represent past from ${context.params.present.iso8601}" }
): ValidationRule<FutureOrPresent.RuleDescriptor<Instant>, Instant, FutureOrPresent<Temporal<Instant>>> =
    ValidationRule.create(FutureOrPresent.RuleDescriptor(), FutureOrPresent { Temporal.from(now()) }, errorMessage) { Temporal.from(it) }

@JvmName("futureOrPresentLocalDate")
fun ValidationRules<LocalDate>.futureOrPresent(
    now: () -> LocalDate = LocalDate::now,
    errorMessage: LazyErrorMessage<FutureOrPresent.RuleDescriptor<LocalDate>, LocalDate, FutureOrPresent<Temporal<LocalDate>>> =
        { "Date must not represent past from ${context.params.present.iso8601}" }
): ValidationRule<FutureOrPresent.RuleDescriptor<LocalDate>, LocalDate, FutureOrPresent<Temporal<LocalDate>>> =
    ValidationRule.create(FutureOrPresent.RuleDescriptor(), FutureOrPresent { Temporal.from(now()) }, errorMessage) { Temporal.from(it) }

@JvmName("futureOrPresentLocalDateTime")
fun ValidationRules<LocalDateTime>.futureOrPresent(
    now: () -> LocalDateTime = LocalDateTime::now,
    errorMessage: LazyErrorMessage<FutureOrPresent.RuleDescriptor<LocalDateTime>, LocalDateTime, FutureOrPresent<Temporal<LocalDateTime>>> =
        { "Date must not represent past from ${context.params.present.iso8601}" }
): ValidationRule<FutureOrPresent.RuleDescriptor<LocalDateTime>, LocalDateTime, FutureOrPresent<Temporal<LocalDateTime>>> =
    ValidationRule.create(FutureOrPresent.RuleDescriptor(), FutureOrPresent { Temporal.from(now()) }, errorMessage) { Temporal.from(it) }

@JvmName("futureOrPresentOffsetDateTime")
fun ValidationRules<OffsetDateTime>.futureOrPresent(
    now: () -> OffsetDateTime = OffsetDateTime::now,
    errorMessage: LazyErrorMessage<FutureOrPresent.RuleDescriptor<OffsetDateTime>, OffsetDateTime, FutureOrPresent<Temporal<OffsetDateTime>>> =
        { "Date must not represent past from ${context.params.present.iso8601}" }
): ValidationRule<FutureOrPresent.RuleDescriptor<OffsetDateTime>, OffsetDateTime, FutureOrPresent<Temporal<OffsetDateTime>>> =
    ValidationRule.create(FutureOrPresent.RuleDescriptor(), FutureOrPresent { Temporal.from(now()) }, errorMessage) { Temporal.from(it) }

@JvmName("futureOrPresentZonedDateTime")
fun ValidationRules<ZonedDateTime>.futureOrPresent(
    now: () -> ZonedDateTime = ZonedDateTime::now,
    errorMessage: LazyErrorMessage<FutureOrPresent.RuleDescriptor<ZonedDateTime>, ZonedDateTime, FutureOrPresent<Temporal<ZonedDateTime>>> =
        { "Date must not represent past from ${context.params.present.iso8601}" }
): ValidationRule<FutureOrPresent.RuleDescriptor<ZonedDateTime>, ZonedDateTime, FutureOrPresent<Temporal<ZonedDateTime>>> =
    ValidationRule.create(FutureOrPresent.RuleDescriptor(), FutureOrPresent { Temporal.from(now()) }, errorMessage) { Temporal.from(it) }

@JvmName("futureOrPresentLocalTime")
fun ValidationRules<LocalTime>.futureOrPresent(
    now: () -> LocalTime = LocalTime::now,
    errorMessage: LazyErrorMessage<FutureOrPresent.RuleDescriptor<LocalTime>, LocalTime, FutureOrPresent<Temporal<LocalTime>>> =
        { "Date must not represent past from ${context.params.present.iso8601}" }
): ValidationRule<FutureOrPresent.RuleDescriptor<LocalTime>, LocalTime, FutureOrPresent<Temporal<LocalTime>>> =
    ValidationRule.create(FutureOrPresent.RuleDescriptor(), FutureOrPresent { Temporal.from(now()) }, errorMessage) { Temporal.from(it) }

@JvmName("futureOrPresentOffsetTime")
fun ValidationRules<OffsetTime>.futureOrPresent(
    now: () -> OffsetTime = OffsetTime::now,
    errorMessage: LazyErrorMessage<FutureOrPresent.RuleDescriptor<OffsetTime>, OffsetTime, FutureOrPresent<Temporal<OffsetTime>>> =
        { "Date must not represent past from ${context.params.present.iso8601}" }
): ValidationRule<FutureOrPresent.RuleDescriptor<OffsetTime>, OffsetTime, FutureOrPresent<Temporal<OffsetTime>>> =
    ValidationRule.create(FutureOrPresent.RuleDescriptor(), FutureOrPresent { Temporal.from(now()) }, errorMessage) { Temporal.from(it) }

/**
 * Interface used to group types that represents temporal values, introduced so that unified
 * checks can be defined.
 *
 * New implementation should be added in a form of factory function defined as an extension of
 * [the companion object][Temporal.Companion] of this interface (see examples for predefined
 * implementations [Temporal.Companion.from]) together with corresponding [rule][ValidationRule]
 * factory function (e.g. [ValidationRules.past]).
 */
interface Temporal<T> : Comparable<Temporal<T>> {
    val value: T
    val iso8601: String

    companion object
}

private fun <T : Comparable<T>> Temporal<T>.compareToImpl(other: Temporal<T>): Int =
    value.compareTo(other.value)

fun Temporal.Companion.from(value: Instant): Temporal<Instant> =
    object : Temporal<Instant> {
        override val value: Instant = value
        override val iso8601: String = DateTimeFormatter.ISO_INSTANT.format(this.value)

        override fun compareTo(other: Temporal<Instant>): Int = compareToImpl(other)
    }

fun Temporal.Companion.from(value: LocalDateTime): Temporal<LocalDateTime> =
    object : Temporal<LocalDateTime> {
        override val value: LocalDateTime = value
        override val iso8601: String = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(this.value)

        override fun compareTo(other: Temporal<LocalDateTime>): Int = compareToImpl(other)
    }

fun Temporal.Companion.from(value: OffsetDateTime): Temporal<OffsetDateTime> =
    object : Temporal<OffsetDateTime> {
        override val value: OffsetDateTime = value
        override val iso8601: String = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this.value)

        override fun compareTo(other: Temporal<OffsetDateTime>): Int = compareToImpl(other)
    }

fun Temporal.Companion.from(value: ZonedDateTime): Temporal<ZonedDateTime> =
    object : Temporal<ZonedDateTime> {
        override val value: ZonedDateTime = value
        override val iso8601: String = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this.value)

        override fun compareTo(other: Temporal<ZonedDateTime>): Int = compareToImpl(other)
    }

fun Temporal.Companion.from(value: LocalDate): Temporal<LocalDate> =
    object : Temporal<LocalDate> {
        override val value: LocalDate = value
        override val iso8601: String = DateTimeFormatter.ISO_LOCAL_DATE.format(this.value)

        override fun compareTo(other: Temporal<LocalDate>): Int = compareToImpl(other)
    }

fun Temporal.Companion.from(value: LocalTime): Temporal<LocalTime> =
    object : Temporal<LocalTime> {
        override val value: LocalTime = value
        override val iso8601: String = DateTimeFormatter.ISO_LOCAL_TIME.format(this.value)

        override fun compareTo(other: Temporal<LocalTime>): Int = compareToImpl(other)
    }

fun Temporal.Companion.from(value: OffsetTime): Temporal<OffsetTime> =
    object : Temporal<OffsetTime> {
        override val value: OffsetTime = value
        override val iso8601: String = DateTimeFormatter.ISO_OFFSET_TIME.format(this.value)

        override fun compareTo(other: Temporal<OffsetTime>): Int = compareToImpl(other)
    }
