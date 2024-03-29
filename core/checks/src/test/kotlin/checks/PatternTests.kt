package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.validation
import io.dwsoft.checkt.testing.forAll
import io.dwsoft.checkt.testing.shouldBeInvalidBecause
import io.dwsoft.checkt.testing.shouldBeValid
import io.dwsoft.checkt.testing.shouldNotPass
import io.dwsoft.checkt.testing.shouldPass
import io.dwsoft.checkt.testing.testValidation
import io.dwsoft.checkt.testing.violated
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.Gen
import io.kotest.property.arbitrary.filterNot
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.exhaustive.flatMap
import io.kotest.property.exhaustive.map
import io.kotest.property.exhaustive.of

class PatternTests : FreeSpec({
    "${Pattern::class.simpleName}" - {
        forAll(casesFor(anyRegex())) {
            "Check works" {
                when {
                    value matches regex -> value shouldPass Pattern(regex)
                    else -> value shouldNotPass Pattern(regex)
                }
            }

            "Rule works" {
                testValidation(
                    of = value,
                    with = validation { +matchesRegex(regex) }
                ) {
                    when {
                        value matches regex -> result.shouldBeValid()
                        else -> result.shouldBeInvalidBecause(
                            validated.violated<Pattern> { msg ->
                                msg shouldContain "Value must match regex '$regex'"
                            }
                        )
                    }
                }
            }
        }
    }
})

private fun casesFor(regexes: Exhaustive<Regex>): Gen<PatternCase> =
    regexes.flatMap {
        Exhaustive.of(
            valueMatching(it),
            valueNotMatching(it),
        )
    }

private fun valueNotMatching(regex: Regex): PatternCase =
    PatternCase(Arb.string().filterNot { it.matches(regex) }.next(), regex)

private fun valueMatching(regex: Regex): PatternCase =
    PatternCase(Arb.stringPattern(regex.toString()).next(), regex)

private fun anyRegex(): Exhaustive<Regex> =
    Exhaustive.of(
        "^\\d*\\.\\d+\$", // decimal numbers
        "^([a-zA-Z0-9._%-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6})*$", // email
        "^(?=(.*[0-9]))((?=.*[A-Za-z0-9])(?=.*[A-Z])(?=.*[a-z])).{8,}$", // std password
        "^https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#()?&/=]*)$", // url
        "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\$", // IPv4
        "^([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))$", // date format YYYY-MM-dd
    ).map {
        it.toRegex()
    }

private data class PatternCase(
    val value: CharSequence,
    val regex: Regex,
)
