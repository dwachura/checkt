package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.testing.testsFor
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Exhaustive
import io.kotest.property.Gen
import io.kotest.property.exhaustive.of
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.Period
import java.time.ZonedDateTime
import kotlin.reflect.KClass

class TemporalTests : FreeSpec({
    testsFor(anyTemporalCase()) {
        taking { temporal } asValue {
            check { Past(case::present) } shouldPassWhen { value < case.present }
            check { PastOrPresent(case::present) } shouldPassWhen { value <= case.present }
            check { Future(case::present) } shouldPassWhen { value > case.present }
            check { FutureOrPresent(case::present) } shouldPassWhen { value >= case.present }
        }
    }

    testsFor(temporalCasesFor(Instant::class)) {
        taking { temporal.value } asValue {
            rule { past({ case.present.value }) } shouldPassWhen { value < case.present.value } orFail
                    { withMessageThat { it shouldContain "Date must represent past from ${case.present.iso8601}" } }
            rule { pastOrPresent({ case.present.value }) } shouldPassWhen { value <= case.present.value } orFail
                    { withMessageThat { it shouldContain "Date must not represent future from ${case.present.iso8601}" } }
            rule { future({ case.present.value }) } shouldPassWhen { value > case.present.value } orFail
                    { withMessageThat { it shouldContain "Date must represent future from ${case.present.iso8601}" } }
            rule { futureOrPresent({ case.present.value }) } shouldPassWhen { value >= case.present.value } orFail
                    { withMessageThat { it shouldContain "Date must not represent past from ${case.present.iso8601}" } }
        }
    }

    testsFor(temporalCasesFor(LocalTime::class)) {
        taking { temporal.value } asValue {
            rule { past({ case.present.value }) } shouldPassWhen { value < case.present.value } orFail
                    { withMessageThat { it shouldContain "Date must represent past from ${case.present.iso8601}" } }
            rule { pastOrPresent({ case.present.value }) } shouldPassWhen { value <= case.present.value } orFail
                    { withMessageThat { it shouldContain "Date must not represent future from ${case.present.iso8601}" } }
            rule { future({ case.present.value }) } shouldPassWhen { value > case.present.value } orFail
                    { withMessageThat { it shouldContain "Date must represent future from ${case.present.iso8601}" } }
            rule { futureOrPresent({ case.present.value }) } shouldPassWhen { value >= case.present.value } orFail
                    { withMessageThat { it shouldContain "Date must not represent past from ${case.present.iso8601}" } }
        }
    }

    testsFor(temporalCasesFor(OffsetTime::class)) {
        taking { temporal.value } asValue {
            rule { past({ case.present.value }) } shouldPassWhen { value < case.present.value } orFail
                    { withMessageThat { it shouldContain "Date must represent past from ${case.present.iso8601}" } }
            rule { pastOrPresent({ case.present.value }) } shouldPassWhen { value <= case.present.value } orFail
                    { withMessageThat { it shouldContain "Date must not represent future from ${case.present.iso8601}" } }
            rule { future({ case.present.value }) } shouldPassWhen { value > case.present.value } orFail
                    { withMessageThat { it shouldContain "Date must represent future from ${case.present.iso8601}" } }
            rule { futureOrPresent({ case.present.value }) } shouldPassWhen { value >= case.present.value } orFail
                    { withMessageThat { it shouldContain "Date must not represent past from ${case.present.iso8601}" } }
        }
    }

    testsFor(temporalCasesFor(LocalDate::class)) {
        taking { temporal.value } asValue {
            rule { past({ case.present.value }) } shouldPassWhen { value < case.present.value } orFail
                    { withMessageThat { it shouldContain "Date must represent past from ${case.present.iso8601}" } }
            rule { pastOrPresent({ case.present.value }) } shouldPassWhen { value <= case.present.value } orFail
                    { withMessageThat { it shouldContain "Date must not represent future from ${case.present.iso8601}" } }
            rule { future({ case.present.value }) } shouldPassWhen { value > case.present.value } orFail
                    { withMessageThat { it shouldContain "Date must represent future from ${case.present.iso8601}" } }
            rule { futureOrPresent({ case.present.value }) } shouldPassWhen { value >= case.present.value } orFail
                    { withMessageThat { it shouldContain "Date must not represent past from ${case.present.iso8601}" } }
        }
    }

    testsFor(temporalCasesFor(LocalDateTime::class)) {
        taking { temporal.value } asValue {
            rule { past({ case.present.value }) } shouldPassWhen { value < case.present.value } orFail
                    { withMessageThat { it shouldContain "Date must represent past from ${case.present.iso8601}" } }
            rule { pastOrPresent({ case.present.value }) } shouldPassWhen { value <= case.present.value } orFail
                    { withMessageThat { it shouldContain "Date must not represent future from ${case.present.iso8601}" } }
            rule { future({ case.present.value }) } shouldPassWhen { value > case.present.value } orFail
                    { withMessageThat { it shouldContain "Date must represent future from ${case.present.iso8601}" } }
            rule { futureOrPresent({ case.present.value }) } shouldPassWhen { value >= case.present.value } orFail
                    { withMessageThat { it shouldContain "Date must not represent past from ${case.present.iso8601}" } }
        }
    }

    testsFor(temporalCasesFor(OffsetDateTime::class)) {
        taking { temporal.value } asValue {
            rule { past({ case.present.value }) } shouldPassWhen { value < case.present.value } orFail
                    { withMessageThat { it shouldContain "Date must represent past from ${case.present.iso8601}" } }
            rule { pastOrPresent({ case.present.value }) } shouldPassWhen { value <= case.present.value } orFail
                    { withMessageThat { it shouldContain "Date must not represent future from ${case.present.iso8601}" } }
            rule { future({ case.present.value }) } shouldPassWhen { value > case.present.value } orFail
                    { withMessageThat { it shouldContain "Date must represent future from ${case.present.iso8601}" } }
            rule { futureOrPresent({ case.present.value }) } shouldPassWhen { value >= case.present.value } orFail
                    { withMessageThat { it shouldContain "Date must not represent past from ${case.present.iso8601}" } }
        }
    }

    testsFor(temporalCasesFor(ZonedDateTime::class)) {
        taking { temporal.value } asValue {
            rule { past({ case.present.value }) } shouldPassWhen { value < case.present.value } orFail
                    { withMessageThat { it shouldContain "Date must represent past from ${case.present.iso8601}" } }
            rule { pastOrPresent({ case.present.value }) } shouldPassWhen { value <= case.present.value } orFail
                    { withMessageThat { it shouldContain "Date must not represent future from ${case.present.iso8601}" } }
            rule { future({ case.present.value }) } shouldPassWhen { value > case.present.value } orFail
                    { withMessageThat { it shouldContain "Date must represent future from ${case.present.iso8601}" } }
            rule { futureOrPresent({ case.present.value }) } shouldPassWhen { value >= case.present.value } orFail
                    { withMessageThat { it shouldContain "Date must not represent past from ${case.present.iso8601}" } }
        }
    }
})

private fun anyTemporalCase(): Gen<TemporalCase<Any>> {
    val present = AnyTemporal()
    val past = present.past
    val future = present.future
    return Exhaustive.of(
        TemporalCase(present, past),
        TemporalCase(present, present),
        TemporalCase(present, future),
    )
}

private data class TemporalCase<T>(
    val present: Temporal<T>,
    val temporal: Temporal<T>,
)

private class AnyTemporal(private val order: Int = 0) : Temporal<Any> {
    override val value: Any = order
    override val iso8601: String = "$order"

    val past get() = AnyTemporal(order - 1)
    val future get() = AnyTemporal(order - 1)

    override fun compareTo(other: Temporal<Any>): Int =
        order.compareTo((other as AnyTemporal).order)
}

@Suppress("UNCHECKED_CAST")
private fun <T : Any> temporalCasesFor(aClass: KClass<T>): Exhaustive<TemporalCase<T>> {
    val `1s` = Duration.ofDays(1)
    val `2s` = Duration.ofDays(2)
    val `1d` = Period.ofDays(1)
    val `2d` = Period.ofDays(2)
    val (past, present, future) = when (aClass) {
        Instant::class -> Instant.now() to { it + `1s` to it + `2s` } mapComponents { Temporal.from(it) }
        LocalTime::class -> LocalTime.now() to { it + `1s` to it + `2s` } mapComponents { Temporal.from(it) }
        OffsetTime::class -> OffsetTime.now() to { it + `1s` to it + `2s` } mapComponents { Temporal.from(it) }
        LocalDateTime::class -> LocalDateTime.now() to { it + `1s` to it + `2s` } mapComponents { Temporal.from(it) }
        OffsetDateTime::class -> OffsetDateTime.now() to { it + `1s` to it + `2s` } mapComponents { Temporal.from(it) }
        ZonedDateTime::class -> ZonedDateTime.now() to { it + `1s` to it + `2s` } mapComponents { Temporal.from(it) }
        LocalDate::class -> LocalDate.now() to { it + `1d` to it + `2d` } mapComponents { Temporal.from(it) }
        else -> throw IllegalArgumentException(
            "Temporal for ${aClass.qualifiedName} is not defined"
        )
    } as Triple<Temporal<T>, Temporal<T>, Temporal<T>>
    return Exhaustive.of(
        TemporalCase(present, past),
        TemporalCase(present, present),
        TemporalCase(present, future),
    )
}

private infix fun <T> T.to(f: (T) -> Pair<T, T>): Triple<T, T, T> =
    f(this).let { Triple(this, it.first, it.second) }

private infix fun <T, R> Triple<T, T, T>.mapComponents(by: (T) -> R): Triple<R, R, R> =
    this.let { Triple(by(it.first), by(it.second), by(it.third)) }
