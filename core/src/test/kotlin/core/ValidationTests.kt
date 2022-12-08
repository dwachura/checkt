package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.Check.Parameterless.Companion.delegate
import io.dwsoft.checkt.testing.AlwaysFailingCheck
import io.dwsoft.checkt.testing.alwaysFailWithMessage
import io.dwsoft.checkt.testing.shouldFailBecause
import io.dwsoft.checkt.testing.shouldRepresentCompletedValidation
import io.dwsoft.checkt.testing.testValidation
import io.dwsoft.checkt.testing.violated
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.property.Arb
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string

class ValidationTests : FreeSpec({
    data class Dto(
        val simpleValue: String = Arb.string().next(),
        val collection: Collection<Number> = Arb.list(Arb.double()).next(),
        val map: Map<Double, String> = Arb.map(Arb.double(), Arb.string()).next(),
    )

    "Validation in root scope" {
        testValidation(
            of = Dto(),
            with = validationSpec {
                +alwaysFailWithMessage { "1" }
                subject.simpleValue require alwaysFailWithMessage { "2" }
            }
        ) {
            result.shouldRepresentCompletedValidation()
                .shouldFailBecause(
                    validated.violated<AlwaysFailingCheck>(withMessage = "1"),
                    validated.simpleValue.violated<AlwaysFailingCheck>(withMessage = "2"),
                )
        }
    }

    "Validation in nested scope" {
        testValidation(
            of = Dto(),
            with = validationSpec {
                subject.simpleValue.namedAs(!"simpleVal") require { +alwaysFailWithMessage { "1" } }
                subject::collection require { +alwaysFailWithMessage { "2" } }
            }
        ) {
            result.shouldRepresentCompletedValidation()
                .shouldFailBecause(
                    validated.simpleValue.violated<AlwaysFailingCheck>(
                        withMessage = "1",
                        underPath = { -"" / "simpleVal" }
                    ),
                    validated.collection.violated<AlwaysFailingCheck>(
                        withMessage = "2",
                        underPath = { -"" / "collection" }
                    ),
                )
        }
    }

    "Elements of iterable properties are validated" {
        val toValidate = Dto()
        val expectedViolations = toValidate.collection.mapIndexed { idx, elem ->
            elem.violated<AlwaysFailingCheck>(
                underPath = { -"" / "collection"[idx.idx] },
                withMessage = "$idx"
            )
        }.toTypedArray()

        testValidation(
            of = toValidate,
            with = validationSpec {
                subject::collection require {
                    eachElement { idx ->
                        subject require alwaysFailWithMessage { "$idx" }
                    }
                }
            }
        ) {
            result.shouldRepresentCompletedValidation()
                .shouldFailBecause(*expectedViolations)
        }
    }

    "Entries of maps are validated" {
        val toValidate = Dto()
        val expectedViolations = toValidate.map.map { (key, value) ->
            val expectedMessage = "$key:$value"
            key.violated<AlwaysFailingCheck>(
                withMessage = expectedMessage,
                underPath = { -"" / "map"["$key"] / "key" }
            )
            value.violated<AlwaysFailingCheck>(
                withMessage = expectedMessage,
                underPath = { -"" / "map"["$key"] / "value" }
            )
        }.toTypedArray()

        testValidation(
            of = toValidate,
            with = validationSpec {
                subject::map require {
                    eachEntry(
                        keyValidation = { value -> +alwaysFailWithMessage { "$subject:$value" } },
                        valueValidation = { key -> +alwaysFailWithMessage { "$key:$subject" } },
                    )
                }
            }
        ) {
            result.shouldRepresentCompletedValidation()
                .shouldFailBecause(*expectedViolations)
        }
    }

    "Exception handling" - {
        val expectedException = RuntimeException("error")

        "Exceptions from validation rules are caught" {
            class ThrowingCheck(val ex: Throwable) :
                Check.Parameterless<Any, ThrowingCheck> by delegate({ throw ex })

            val spec = validationSpec<Dto> {
                subject::simpleValue require {
                    +ThrowingCheck(expectedException).toValidationRule { "" }
                }
            }

            testValidation(Dto(), spec) {
                result.shouldBeFailure(expectedException)
            }
        }

        "Exceptions from nested scopes are caught" {
            val spec = validationSpec<Dto> {
                subject::simpleValue require {
                    subject::length require {
                        subject.namedAs(!"deeper") require { throw expectedException }
                    }
                }
            }

            testValidation(Dto(), spec) {
                result.shouldBeFailure(expectedException)
            }
        }
    }
})
