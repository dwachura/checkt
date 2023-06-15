package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ParameterizedCheck
import io.dwsoft.checkt.core.ParameterizedCheck.Result
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
import io.dwsoft.checkt.core.checks.Temporal.Companion as Temporals

class Past<V : Temporal<*>>(private val present: () -> V) : ParameterizedCheck<V, Past.Params<V>> {
    override suspend fun invoke(value: V): Result<Params<V>> =
        present().let { Result(compareValues(value, it) < 0, Params(it)) }

    data class Params<V : Temporal<*>>(val present: V) : ParamsOf<Past<V>, Params<V>>

    class Rule<V> : ValidationRule.Descriptor<V, Past<Temporal<V>>, Rule<V>> {
        override val defaultMessage: LazyErrorMessage<Rule<V>, V> =
            { "Date must represent past from ${context.params.present.iso8601}" }
    }
}

@JvmName("pastInstant")
fun ValidationRules<Instant>.past(
    now: () -> Instant = Instant::now,
): ValidationRule<Past.Rule<Instant>, Instant> =
    ValidationRule.create(Past.Rule(), Past { Temporals.from(now()) }, Temporals::from)

@JvmName("pastLocalDate")
fun ValidationRules<LocalDate>.past(
    now: () -> LocalDate = LocalDate::now,
): ValidationRule<Past.Rule<LocalDate>, LocalDate> =
    ValidationRule.create(Past.Rule(), Past { Temporals.from(now()) }, Temporals::from)

@JvmName("pastLocalDateTime")
fun ValidationRules<LocalDateTime>.past(
    now: () -> LocalDateTime = LocalDateTime::now,
): ValidationRule<Past.Rule<LocalDateTime>, LocalDateTime> =
    ValidationRule.create(Past.Rule(), Past { Temporals.from(now()) }, Temporals::from)

@JvmName("pastOffsetDateTime")
fun ValidationRules<OffsetDateTime>.past(
    now: () -> OffsetDateTime = OffsetDateTime::now,
): ValidationRule<Past.Rule<OffsetDateTime>, OffsetDateTime> =
    ValidationRule.create(Past.Rule(), Past { Temporals.from(now()) }, Temporals::from)

@JvmName("pastZonedDateTime")
fun ValidationRules<ZonedDateTime>.past(
    now: () -> ZonedDateTime = ZonedDateTime::now,
): ValidationRule<Past.Rule<ZonedDateTime>, ZonedDateTime> =
    ValidationRule.create(Past.Rule(), Past { Temporals.from(now()) }, Temporals::from)

@JvmName("pastLocalTime")
fun ValidationRules<LocalTime>.past(
    now: () -> LocalTime = LocalTime::now,
): ValidationRule<Past.Rule<LocalTime>, LocalTime> =
    ValidationRule.create(Past.Rule(), Past { Temporals.from(now()) }, Temporals::from)

@JvmName("pastOffsetTime")
fun ValidationRules<OffsetTime>.past(
    now: () -> OffsetTime = OffsetTime::now,
): ValidationRule<Past.Rule<OffsetTime>, OffsetTime> =
    ValidationRule.create(Past.Rule(), Past { Temporals.from(now()) }, Temporals::from)

class PastOrPresent<V : Temporal<*>>(
    private val present: () -> V
) : ParameterizedCheck<V, PastOrPresent.Params<V>> {
    override suspend fun invoke(value: V): Result<Params<V>> =
        present().let { Result(compareValues(value, it) <= 0, Params(it)) }

    data class Params<V : Temporal<*>>(val present: V) : ParamsOf<PastOrPresent<V>, Params<V>>

    class Rule<V> : ValidationRule.Descriptor<V, PastOrPresent<Temporal<V>>, Rule<V>> {
        override val defaultMessage: LazyErrorMessage<Rule<V>, V> =
            { "Date must not represent future from ${context.params.present.iso8601}" }
    }
}

@JvmName("pastOrPresentInstant")
fun ValidationRules<Instant>.pastOrPresent(
    now: () -> Instant = Instant::now,
): ValidationRule<PastOrPresent.Rule<Instant>, Instant> =
    ValidationRule.create(PastOrPresent.Rule(), PastOrPresent { Temporals.from(now()) }, Temporals::from)

@JvmName("pastOrPresentLocalDate")
fun ValidationRules<LocalDate>.pastOrPresent(
    now: () -> LocalDate = LocalDate::now,
): ValidationRule<PastOrPresent.Rule<LocalDate>, LocalDate> =
    ValidationRule.create(PastOrPresent.Rule(), PastOrPresent { Temporals.from(now()) }, Temporals::from)

@JvmName("pastOrPresentLocalDateTime")
fun ValidationRules<LocalDateTime>.pastOrPresent(
    now: () -> LocalDateTime = LocalDateTime::now,
): ValidationRule<PastOrPresent.Rule<LocalDateTime>, LocalDateTime> =
    ValidationRule.create(PastOrPresent.Rule(), PastOrPresent { Temporals.from(now()) }, Temporals::from)

@JvmName("pastOrPresentOffsetDateTime")
fun ValidationRules<OffsetDateTime>.pastOrPresent(
    now: () -> OffsetDateTime = OffsetDateTime::now,
): ValidationRule<PastOrPresent.Rule<OffsetDateTime>, OffsetDateTime> =
    ValidationRule.create(PastOrPresent.Rule(), PastOrPresent { Temporals.from(now()) }, Temporals::from)

@JvmName("pastOrPresentZonedDateTime")
fun ValidationRules<ZonedDateTime>.pastOrPresent(
    now: () -> ZonedDateTime = ZonedDateTime::now,
): ValidationRule<PastOrPresent.Rule<ZonedDateTime>, ZonedDateTime> =
    ValidationRule.create(PastOrPresent.Rule(), PastOrPresent { Temporals.from(now()) }, Temporals::from)

@JvmName("pastOrPresentLocalTime")
fun ValidationRules<LocalTime>.pastOrPresent(
    now: () -> LocalTime = LocalTime::now,
): ValidationRule<PastOrPresent.Rule<LocalTime>, LocalTime> =
    ValidationRule.create(PastOrPresent.Rule(), PastOrPresent { Temporals.from(now()) }, Temporals::from)

@JvmName("pastOrPresentOffsetTime")
fun ValidationRules<OffsetTime>.pastOrPresent(
    now: () -> OffsetTime = OffsetTime::now,
): ValidationRule<PastOrPresent.Rule<OffsetTime>, OffsetTime> =
    ValidationRule.create(PastOrPresent.Rule(), PastOrPresent { Temporals.from(now()) }, Temporals::from)

class Future<V : Temporal<*>>(private val present: () -> V) : ParameterizedCheck<V, Future.Params<V>> {
    override suspend fun invoke(value: V): Result<Params<V>> =
        present().let { Result(compareValues(value, it) > 0, Params(it)) }

    data class Params<V : Temporal<*>>(val present: V) : ParamsOf<Future<V>, Params<V>>

    class Rule<V> : ValidationRule.Descriptor<V, Future<Temporal<V>>, Rule<V>> {
        override val defaultMessage: LazyErrorMessage<Rule<V>, V> =
            { "Date must represent future from ${context.params.present.iso8601}" }
    }
}

@JvmName("futureInstant")
fun ValidationRules<Instant>.future(
    now: () -> Instant = Instant::now,
): ValidationRule<Future.Rule<Instant>, Instant> =
    ValidationRule.create(Future.Rule(), Future { Temporals.from(now()) }, Temporals::from)

@JvmName("futureLocalDate")
fun ValidationRules<LocalDate>.future(
    now: () -> LocalDate = LocalDate::now,
): ValidationRule<Future.Rule<LocalDate>, LocalDate> =
    ValidationRule.create(Future.Rule(), Future { Temporals.from(now()) }, Temporals::from)

@JvmName("futureLocalDateTime")
fun ValidationRules<LocalDateTime>.future(
    now: () -> LocalDateTime = LocalDateTime::now,
): ValidationRule<Future.Rule<LocalDateTime>, LocalDateTime> =
    ValidationRule.create(Future.Rule(), Future { Temporals.from(now()) }, Temporals::from)

@JvmName("futureOffsetDateTime")
fun ValidationRules<OffsetDateTime>.future(
    now: () -> OffsetDateTime = OffsetDateTime::now,
): ValidationRule<Future.Rule<OffsetDateTime>, OffsetDateTime> =
    ValidationRule.create(Future.Rule(), Future { Temporals.from(now()) }, Temporals::from)

@JvmName("futureZonedDateTime")
fun ValidationRules<ZonedDateTime>.future(
    now: () -> ZonedDateTime = ZonedDateTime::now,
): ValidationRule<Future.Rule<ZonedDateTime>, ZonedDateTime> =
    ValidationRule.create(Future.Rule(), Future { Temporals.from(now()) }, Temporals::from)

@JvmName("futureLocalTime")
fun ValidationRules<LocalTime>.future(
    now: () -> LocalTime = LocalTime::now,
): ValidationRule<Future.Rule<LocalTime>, LocalTime> =
    ValidationRule.create(Future.Rule(), Future { Temporals.from(now()) }, Temporals::from)

@JvmName("futureOffsetTime")
fun ValidationRules<OffsetTime>.future(
    now: () -> OffsetTime = OffsetTime::now,
): ValidationRule<Future.Rule<OffsetTime>, OffsetTime> =
    ValidationRule.create(Future.Rule(), Future { Temporals.from(now()) }, Temporals::from)

class FutureOrPresent<V : Temporal<*>>(
    private val present: () -> V
) : ParameterizedCheck<V, FutureOrPresent.Params<V>> {
    override suspend fun invoke(value: V): Result<Params<V>> =
        present().let { Result(compareValues(value, it) >= 0, Params(it)) }

    data class Params<V : Temporal<*>>(val present: V) : ParamsOf<FutureOrPresent<V>, Params<V>>

    class Rule<V> : ValidationRule.Descriptor<V, FutureOrPresent<Temporal<V>>, Rule<V>> {
        override val defaultMessage: LazyErrorMessage<Rule<V>, V> =
            { "Date must not represent past from ${context.params.present.iso8601}" }
    }
}

@JvmName("futureOrPresentInstant")
fun ValidationRules<Instant>.futureOrPresent(
    now: () -> Instant = Instant::now,
): ValidationRule<FutureOrPresent.Rule<Instant>, Instant> =
    ValidationRule.create(FutureOrPresent.Rule(), FutureOrPresent { Temporals.from(now()) }, Temporals::from)

@JvmName("futureOrPresentLocalDate")
fun ValidationRules<LocalDate>.futureOrPresent(
    now: () -> LocalDate = LocalDate::now,
): ValidationRule<FutureOrPresent.Rule<LocalDate>, LocalDate> =
    ValidationRule.create(FutureOrPresent.Rule(), FutureOrPresent { Temporals.from(now()) }, Temporals::from)

@JvmName("futureOrPresentLocalDateTime")
fun ValidationRules<LocalDateTime>.futureOrPresent(
    now: () -> LocalDateTime = LocalDateTime::now,
): ValidationRule<FutureOrPresent.Rule<LocalDateTime>, LocalDateTime> =
    ValidationRule.create(FutureOrPresent.Rule(), FutureOrPresent { Temporals.from(now()) }, Temporals::from)

@JvmName("futureOrPresentOffsetDateTime")
fun ValidationRules<OffsetDateTime>.futureOrPresent(
    now: () -> OffsetDateTime = OffsetDateTime::now,
): ValidationRule<FutureOrPresent.Rule<OffsetDateTime>, OffsetDateTime> =
    ValidationRule.create(FutureOrPresent.Rule(), FutureOrPresent { Temporals.from(now()) }, Temporals::from)

@JvmName("futureOrPresentZonedDateTime")
fun ValidationRules<ZonedDateTime>.futureOrPresent(
    now: () -> ZonedDateTime = ZonedDateTime::now,
): ValidationRule<FutureOrPresent.Rule<ZonedDateTime>, ZonedDateTime> =
    ValidationRule.create(FutureOrPresent.Rule(), FutureOrPresent { Temporals.from(now()) }, Temporals::from)

@JvmName("futureOrPresentLocalTime")
fun ValidationRules<LocalTime>.futureOrPresent(
    now: () -> LocalTime = LocalTime::now,
): ValidationRule<FutureOrPresent.Rule<LocalTime>, LocalTime> =
    ValidationRule.create(FutureOrPresent.Rule(), FutureOrPresent { Temporals.from(now()) }, Temporals::from)

@JvmName("futureOrPresentOffsetTime")
fun ValidationRules<OffsetTime>.futureOrPresent(
    now: () -> OffsetTime = OffsetTime::now,
): ValidationRule<FutureOrPresent.Rule<OffsetTime>, OffsetTime> =
    ValidationRule.create(FutureOrPresent.Rule(), FutureOrPresent { Temporals.from(now()) }, Temporals::from)

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

fun Temporals.from(value: Instant): Temporal<Instant> =
    object : Temporal<Instant> {
        override val value: Instant = value
        override val iso8601: String = DateTimeFormatter.ISO_INSTANT.format(this.value)

        override fun compareTo(other: Temporal<Instant>): Int = compareToImpl(other)
    }

fun Temporals.from(value: LocalDateTime): Temporal<LocalDateTime> =
    object : Temporal<LocalDateTime> {
        override val value: LocalDateTime = value
        override val iso8601: String = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(this.value)

        override fun compareTo(other: Temporal<LocalDateTime>): Int = compareToImpl(other)
    }

fun Temporals.from(value: OffsetDateTime): Temporal<OffsetDateTime> =
    object : Temporal<OffsetDateTime> {
        override val value: OffsetDateTime = value
        override val iso8601: String = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this.value)

        override fun compareTo(other: Temporal<OffsetDateTime>): Int = compareToImpl(other)
    }

fun Temporals.from(value: ZonedDateTime): Temporal<ZonedDateTime> =
    object : Temporal<ZonedDateTime> {
        override val value: ZonedDateTime = value
        override val iso8601: String = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this.value)

        override fun compareTo(other: Temporal<ZonedDateTime>): Int = compareToImpl(other)
    }

fun Temporals.from(value: LocalDate): Temporal<LocalDate> =
    object : Temporal<LocalDate> {
        override val value: LocalDate = value
        override val iso8601: String = DateTimeFormatter.ISO_LOCAL_DATE.format(this.value)

        override fun compareTo(other: Temporal<LocalDate>): Int = compareToImpl(other)
    }

fun Temporals.from(value: LocalTime): Temporal<LocalTime> =
    object : Temporal<LocalTime> {
        override val value: LocalTime = value
        override val iso8601: String = DateTimeFormatter.ISO_LOCAL_TIME.format(this.value)

        override fun compareTo(other: Temporal<LocalTime>): Int = compareToImpl(other)
    }

fun Temporals.from(value: OffsetTime): Temporal<OffsetTime> =
    object : Temporal<OffsetTime> {
        override val value: OffsetTime = value
        override val iso8601: String = DateTimeFormatter.ISO_OFFSET_TIME.format(this.value)

        override fun compareTo(other: Temporal<OffsetTime>): Int = compareToImpl(other)
    }
