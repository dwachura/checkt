package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.testing.testsFor
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Exhaustive
import io.kotest.property.Gen
import io.kotest.property.exhaustive.of
import io.kotest.property.exhaustive.plus
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZonedDateTime

// TODO: clean up

class TemporalTests : FreeSpec({
    testsFor(temporalCases<Instant>()) {
        fromCase(take = { temporal }) {
            check { Past(case.present) } shouldPassWhen { value < case.present() }
            check { PastOrPresent(case.present) } shouldPassWhen { value <= case.present() }
            check { Future(case.present) } shouldPassWhen { value > case.present() }
            check { FutureOrPresent(case.present) } shouldPassWhen { value >= case.present() }
        }

        fromCase(take = { temporal.value }) {
            rule { past({ case.present().value }) } shouldPassWhen { value < case.present().value } orFail
                    { withMessageThat { it shouldContain "Date must represent past from ${case.present().iso8601}" } }
            rule { pastOrPresent({ case.present().value }) } shouldPassWhen { value <= case.present().value } orFail
                    { withMessageThat { it shouldContain "Date must not represent future from ${case.present().iso8601}" } }
            rule { future({ case.present().value }) } shouldPassWhen { value > case.present().value } orFail
                    { withMessageThat { it shouldContain "Date must represent future from ${case.present().iso8601}" } }
            rule { futureOrPresent({ case.present().value }) } shouldPassWhen { value >= case.present().value } orFail
                    { withMessageThat { it shouldContain "Date must not represent past from ${case.present().iso8601}" } }
        }
    }

    testsFor(temporalCases<LocalTime>()) {
        fromCase(take = { temporal }) {
            check { Past(case.present) } shouldPassWhen { value < case.present() }
            check { PastOrPresent(case.present) } shouldPassWhen { value <= case.present() }
            check { Future(case.present) } shouldPassWhen { value > case.present() }
            check { FutureOrPresent(case.present) } shouldPassWhen { value >= case.present() }
        }

        fromCase(take = { temporal.value }) {
            rule { past({ case.present().value }) } shouldPassWhen { value < case.present().value } orFail
                    { withMessageThat { it shouldContain "Date must represent past from ${case.present().iso8601}" } }
            rule { pastOrPresent({ case.present().value }) } shouldPassWhen { value <= case.present().value } orFail
                    { withMessageThat { it shouldContain "Date must not represent future from ${case.present().iso8601}" } }
            rule { future({ case.present().value }) } shouldPassWhen { value > case.present().value } orFail
                    { withMessageThat { it shouldContain "Date must represent future from ${case.present().iso8601}" } }
            rule { futureOrPresent({ case.present().value }) } shouldPassWhen { value >= case.present().value } orFail
                    { withMessageThat { it shouldContain "Date must not represent past from ${case.present().iso8601}" } }
        }
    }

    testsFor(temporalCases<OffsetTime>()) {
        fromCase(take = { temporal }) {
            check { Past(case.present) } shouldPassWhen { value < case.present() }
            check { PastOrPresent(case.present) } shouldPassWhen { value <= case.present() }
            check { Future(case.present) } shouldPassWhen { value > case.present() }
            check { FutureOrPresent(case.present) } shouldPassWhen { value >= case.present() }
        }

        fromCase(take = { temporal.value }) {
            rule { past({ case.present().value }) } shouldPassWhen { value < case.present().value } orFail
                    { withMessageThat { it shouldContain "Date must represent past from ${case.present().iso8601}" } }
            rule { pastOrPresent({ case.present().value }) } shouldPassWhen { value <= case.present().value } orFail
                    { withMessageThat { it shouldContain "Date must not represent future from ${case.present().iso8601}" } }
            rule { future({ case.present().value }) } shouldPassWhen { value > case.present().value } orFail
                    { withMessageThat { it shouldContain "Date must represent future from ${case.present().iso8601}" } }
            rule { futureOrPresent({ case.present().value }) } shouldPassWhen { value >= case.present().value } orFail
                    { withMessageThat { it shouldContain "Date must not represent past from ${case.present().iso8601}" } }
        }
    }

    testsFor(temporalCases<LocalDate>()) {
        fromCase(take = { temporal }) {
            check { Past(case.present) } shouldPassWhen { value < case.present() }
            check { PastOrPresent(case.present) } shouldPassWhen { value <= case.present() }
            check { Future(case.present) } shouldPassWhen { value > case.present() }
            check { FutureOrPresent(case.present) } shouldPassWhen { value >= case.present() }
        }

        fromCase(take = { temporal.value }) {
            rule { past({ case.present().value }) } shouldPassWhen { value < case.present().value } orFail
                    { withMessageThat { it shouldContain "Date must represent past from ${case.present().iso8601}" } }
            rule { pastOrPresent({ case.present().value }) } shouldPassWhen { value <= case.present().value } orFail
                    { withMessageThat { it shouldContain "Date must not represent future from ${case.present().iso8601}" } }
            rule { future({ case.present().value }) } shouldPassWhen { value > case.present().value } orFail
                    { withMessageThat { it shouldContain "Date must represent future from ${case.present().iso8601}" } }
            rule { futureOrPresent({ case.present().value }) } shouldPassWhen { value >= case.present().value } orFail
                    { withMessageThat { it shouldContain "Date must not represent past from ${case.present().iso8601}" } }
        }
    }

    testsFor(temporalCases<LocalDateTime>()) {
        fromCase(take = { temporal }) {
            check { Past(case.present) } shouldPassWhen { value < case.present() }
            check { PastOrPresent(case.present) } shouldPassWhen { value <= case.present() }
            check { Future(case.present) } shouldPassWhen { value > case.present() }
            check { FutureOrPresent(case.present) } shouldPassWhen { value >= case.present() }
        }

        fromCase(take = { temporal.value }) {
            rule { past({ case.present().value }) } shouldPassWhen { value < case.present().value } orFail
                    { withMessageThat { it shouldContain "Date must represent past from ${case.present().iso8601}" } }
            rule { pastOrPresent({ case.present().value }) } shouldPassWhen { value <= case.present().value } orFail
                    { withMessageThat { it shouldContain "Date must not represent future from ${case.present().iso8601}" } }
            rule { future({ case.present().value }) } shouldPassWhen { value > case.present().value } orFail
                    { withMessageThat { it shouldContain "Date must represent future from ${case.present().iso8601}" } }
            rule { futureOrPresent({ case.present().value }) } shouldPassWhen { value >= case.present().value } orFail
                    { withMessageThat { it shouldContain "Date must not represent past from ${case.present().iso8601}" } }
        }
    }

    testsFor(temporalCases<OffsetDateTime>()) {
        fromCase(take = { temporal }) {
            check { Past(case.present) } shouldPassWhen { value < case.present() }
            check { PastOrPresent(case.present) } shouldPassWhen { value <= case.present() }
            check { Future(case.present) } shouldPassWhen { value > case.present() }
            check { FutureOrPresent(case.present) } shouldPassWhen { value >= case.present() }
        }

        fromCase(take = { temporal.value }) {
            rule { past({ case.present().value }) } shouldPassWhen { value < case.present().value } orFail
                    { withMessageThat { it shouldContain "Date must represent past from ${case.present().iso8601}" } }
            rule { pastOrPresent({ case.present().value }) } shouldPassWhen { value <= case.present().value } orFail
                    { withMessageThat { it shouldContain "Date must not represent future from ${case.present().iso8601}" } }
            rule { future({ case.present().value }) } shouldPassWhen { value > case.present().value } orFail
                    { withMessageThat { it shouldContain "Date must represent future from ${case.present().iso8601}" } }
            rule { futureOrPresent({ case.present().value }) } shouldPassWhen { value >= case.present().value } orFail
                    { withMessageThat { it shouldContain "Date must not represent past from ${case.present().iso8601}" } }
        }
    }

    testsFor(temporalCases<ZonedDateTime>()) {
        fromCase(take = { temporal }) {
            check { Past(case.present) } shouldPassWhen { value < case.present() }
            check { PastOrPresent(case.present) } shouldPassWhen { value <= case.present() }
            check { Future(case.present) } shouldPassWhen { value > case.present() }
            check { FutureOrPresent(case.present) } shouldPassWhen { value >= case.present() }
        }

        fromCase(take = { temporal.value }) {
            rule { past({ case.present().value }) } shouldPassWhen { value < case.present().value } orFail
                    { withMessageThat { it shouldContain "Date must represent past from ${case.present().iso8601}" } }
            rule { pastOrPresent({ case.present().value }) } shouldPassWhen { value <= case.present().value } orFail
                    { withMessageThat { it shouldContain "Date must not represent future from ${case.present().iso8601}" } }
            rule { future({ case.present().value }) } shouldPassWhen { value > case.present().value } orFail
                    { withMessageThat { it shouldContain "Date must represent future from ${case.present().iso8601}" } }
            rule { futureOrPresent({ case.present().value }) } shouldPassWhen { value >= case.present().value } orFail
                    { withMessageThat { it shouldContain "Date must not represent past from ${case.present().iso8601}" } }
        }
    }
})

private fun allTemporalCases(): Gen<TemporalCase<*>> =
    temporalCases<Instant>() +
            temporalCases<LocalTime>() +
            temporalCases<OffsetTime>() +
            temporalCases<LocalDate>() +
            temporalCases<LocalDateTime>() +
            temporalCases<OffsetDateTime>() +
            temporalCases<ZonedDateTime>()

private inline fun <reified T : Comparable<T>> temporalCases(): Exhaustive<TemporalCase<T>> {
    val present: Temporal<T> = when (val clazz = T::class) {
        Instant::class -> present<Instant>()
        LocalTime::class -> present<LocalTime>()
        OffsetTime::class -> present<OffsetTime>()
        LocalDate::class -> present<LocalDate>()
        LocalDateTime::class -> present<LocalDateTime>()
        OffsetDateTime::class -> present<OffsetDateTime>()
        ZonedDateTime::class -> present<ZonedDateTime>()
        else -> throw IllegalArgumentException(
            "Temporal for ${clazz.qualifiedName} is not defined"
        )
    } as Temporal<T>
    return Exhaustive.of(
        TemporalCase(present.past()) { present },
        TemporalCase(present) { present },
        TemporalCase(present.future()) { present },
    )
}

private data class TemporalCase<T : Comparable<T>>(
    val temporal: Temporal<T>,
    val present: () -> Temporal<T>,
)

private inline fun <reified T : Comparable<T>> present(): Temporal<T> =
    when (val clazz = T::class) {
        Instant::class -> Temporal.of(Instant.now())
        LocalTime::class -> Temporal.of(LocalTime.now())
        OffsetTime::class -> Temporal.of(OffsetTime.now())
        LocalDate::class -> Temporal.of(LocalDate.now())
        LocalDateTime::class -> Temporal.of(LocalDateTime.now())
        OffsetDateTime::class -> Temporal.of(OffsetDateTime.now())
        ZonedDateTime::class -> Temporal.of(ZonedDateTime.now())
        else -> throw IllegalArgumentException(
            "Temporal for ${clazz.qualifiedName} is not defined"
        )
    } as Temporal<T>

private fun <T : Comparable<T>> Temporal<T>.past(): Temporal<T> =
    when (value) {
        is Instant -> Temporal.of((value as Instant).minusSeconds(60))
        is LocalTime -> Temporal.of((value as LocalTime).minusSeconds(60))
        is OffsetTime -> Temporal.of((value as OffsetTime).minusSeconds(60))
        is LocalDate -> Temporal.of((value as LocalDate).minusDays(1))
        is LocalDateTime -> Temporal.of((value as LocalDateTime).minusDays(1))
        is OffsetDateTime -> Temporal.of((value as OffsetDateTime).minusDays(1))
        is ZonedDateTime -> Temporal.of((value as ZonedDateTime).minusDays(1))
        else -> throw IllegalArgumentException(
            "Temporal for ${value::class.qualifiedName} is not defined"
        )
    } as Temporal<T>

private fun <T : Comparable<T>> Temporal<T>.future(): Temporal<T> =
    when (value) {
        is Instant -> Temporal.of((value as Instant).plusSeconds(60))
        is LocalTime -> Temporal.of((value as LocalTime).plusSeconds(60))
        is OffsetTime -> Temporal.of((value as OffsetTime).plusSeconds(60))
        is LocalDate -> Temporal.of((value as LocalDate).plusDays(1))
        is LocalDateTime -> Temporal.of((value as LocalDateTime).plusDays(1))
        is OffsetDateTime -> Temporal.of((value as OffsetDateTime).plusDays(1))
        is ZonedDateTime -> Temporal.of((value as ZonedDateTime).plusDays(1))
        else -> throw IllegalArgumentException(
            "Temporal for ${value::class.qualifiedName} is not defined"
        )
    } as Temporal<T>
