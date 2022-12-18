package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.Check.Parameterless.Companion.delegate
import io.dwsoft.checkt.testing.failWithMessage
import io.dwsoft.checkt.testing.failed
import io.dwsoft.checkt.testing.pass
import io.dwsoft.checkt.testing.path
import io.dwsoft.checkt.testing.shouldBeInvalid
import io.dwsoft.checkt.testing.shouldBeInvalidBecause
import io.dwsoft.checkt.testing.shouldPass
import io.dwsoft.checkt.testing.shouldRepresentCompletedValidation
import io.dwsoft.checkt.testing.testValidation
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.property.Arb
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string

// TODO: refactor + add proper tests

class ValidationTests : FreeSpec({
    data class Dto(
        val simpleValue: String = Arb.string().next(),
        val collection: Collection<Number> = Arb.list(Arb.double(), 2..3).next(),
        val map: Map<Double, String> = Arb.map(Arb.double(), Arb.string(), minSize = 2, maxSize = 3).next(),
    )

    "Validation in root block" {
        testValidation(
            of = Dto(),
            with = validation {
                (+failWithMessage { "1" }).shouldBeInvalid().violations shouldHaveSize 1
                (+failWithMessage { "2" }).shouldBeInvalid().violations shouldHaveSize 1
                (subject.simpleValue {
                    +failWithMessage { "3" }
                    +failWithMessage { "4" }
                    +failWithMessage { "5" }
                }).also {
                    it.shouldBeInvalid().violations shouldHaveSize 3
                }
            }
        ) {
            result.shouldRepresentCompletedValidation()
                .shouldBeInvalidBecause(
                    validated.failed(withMessage = "1"),
                    validated.failed(withMessage = "2"),
                    validated.simpleValue.failed(withMessage = "3"),
                    validated.simpleValue.failed(withMessage = "4"),
                    validated.simpleValue.failed(withMessage = "5"),
                ).violations shouldHaveSize 5
        }
    }

    "Validation in nested block" {
        testValidation(
            of = Dto(),
            with = validation {
                subject.simpleValue.namedAs(!"simpleVal") require { +failWithMessage { "1" } }
                subject::collection require { +failWithMessage { "2" } }
            }
        ) {
            result.shouldRepresentCompletedValidation()
                .shouldBeInvalidBecause(
                    validated.simpleValue.failed(
                        withMessage = "1",
                        underPath = { -"simpleVal" }
                    ),
                    validated.collection.failed(
                        withMessage = "2",
                        underPath = { -"collection" }
                    ),
                )
        }
    }

    "Elements of iterable properties are validated" {
        val toValidate = Dto()
        val expectedViolations = toValidate.collection.mapIndexed { idx, elem ->
            elem.failed(
                underPath = { -"collection"[idx.idx] },
                withMessage = "$idx"
            )
        }.toTypedArray()

        testValidation(
            of = toValidate,
            with = validation {
                subject::collection require {
                    eachElement { idx ->
                        subject { +failWithMessage { "$idx" } }
                    }
                }
            }
        ) {
            result.shouldRepresentCompletedValidation()
                .shouldBeInvalidBecause(*expectedViolations)
        }
    }

    "Entries of maps are validated" {
        val toValidate = Dto()
        val expectedViolations = toValidate.map.map { (key, value) ->
            val expectedMessage = "$key:$value"
            key.failed(
                withMessage = expectedMessage,
                underPath = { -"map"["$key"] / "key" }
            )
            value.failed(
                withMessage = expectedMessage,
                underPath = { -"map"["$key"] / "value" }
            )
        }.toTypedArray()

        testValidation(
            of = toValidate,
            with = validation {
                subject::map require {
                    eachEntry(
                        keyValidation = { value -> +failWithMessage { "$subject:$value" } },
                        valueValidation = { key -> +failWithMessage { "$key:$subject" } },
                    )
                }
            }
        ) {
            result.shouldRepresentCompletedValidation()
                .shouldBeInvalidBecause(*expectedViolations)
        }
    }

    "Exception handling" - {
        val expectedException = RuntimeException("error")

        "Exceptions from validation rules are caught" {
            class ThrowingCheck(val ex: Throwable) :
                Check.Parameterless<Any, ThrowingCheck> by delegate({ throw ex })

            val spec = validation<Dto> {
                subject::simpleValue require {
                    +ThrowingCheck(expectedException).toValidationRule { "" }
                }
            }

            testValidation(Dto(), spec) {
                result.shouldBeFailure(expectedException)
            }
        }

        "Exceptions from nested blocks are caught" {
            val spec = validation<Dto> {
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

        "Exceptions from nested blocks are caught" {
            val spec = validation<Dto> {
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

        "Exceptional operations can be recovered into validation blocks" {
            val expectedMessage = "exception msg"
            val spec = validation<Dto> {
                val failingResult = subject::simpleValue require {
                    subject::length require { throw IllegalStateException(expectedMessage) }
                }
                failingResult.recoverFrom<RuntimeException> { +pass }
            }

            testValidation(Dto(), spec) {
                result.shouldPass()
            }
        }
    }

    "Rules are processed conditionally" {
        val failFirst = "fail"
        val spec = validation<Dto> {
            val result = if (subject.simpleValue === failFirst) +failWithMessage { "1" } else +pass
            result.whenValid { +failWithMessage { "2" } }
        }

        testValidation(Dto(simpleValue = failFirst), spec) {
            result.shouldBeInvalidBecause(validated.failed(withMessage = "1"))
        }
        testValidation(Dto(), spec) {
            result.shouldBeInvalidBecause(validated.failed(withMessage = "2"))
        }
    }
})


class ValidationOfTests : FreeSpec({
    data class Dto(
        val simpleValue: String = Arb.string().next(),
        val collection: Collection<Number> = Arb.list(Arb.double(), 2..3).next(),
        val map: Map<Double, String> = Arb.map(Arb.double(), Arb.string(), minSize = 2, maxSize = 3).next(),
    )

    "Validation in root block" {
        testValidation(
            of = Dto(),
            with = validation {
                (+failWithMessage { "1" }).shouldBeInvalid().violations shouldHaveSize 1
                (+failWithMessage { "2" }).shouldBeInvalid().violations shouldHaveSize 1
                (subject.simpleValue {
                    +failWithMessage { "3" }
                    +failWithMessage { "4" }
                    +failWithMessage { "5" }
                }).also {
                    it.shouldBeInvalid().violations shouldHaveSize 3
                }
            }
        ) {
            result.shouldRepresentCompletedValidation()
                .shouldBeInvalidBecause(
//                    validated.failed(withMessage = "1"),
//                    validated.simpleValue.failed(withMessage = "2"),
                ).violations shouldHaveSize 5
        }
    }

    "Validation in nested block" {
        testValidation(
            of = Dto(),
            with = validation {
                subject.simpleValue.namedAs(!"simpleVal") require { +failWithMessage { "1" } }
                subject::collection require { +failWithMessage { "2" } }
            }
        ) {
            result.shouldRepresentCompletedValidation()
                .shouldBeInvalidBecause(
                    validated.simpleValue.failed(
                        withMessage = "1",
                        underPath = { -"simpleVal" }
                    ),
                    validated.collection.failed(
                        withMessage = "2",
                        underPath = { -"collection" }
                    ),
                )
        }
    }

    "Elements of iterable properties are validated" {
        val toValidate = Dto()
        val expectedViolations = toValidate.collection.mapIndexed { idx, elem ->
            elem.failed(
                underPath = { -"collection"[idx.idx] },
                withMessage = "$idx"
            )
        }.toTypedArray()

        testValidation(
            of = toValidate,
            with = validation {
                subject::collection require {
                    eachElement { idx ->
                        subject { +failWithMessage { "$idx" } }
                    }
                }
            }
        ) {
            result.shouldRepresentCompletedValidation()
                .shouldBeInvalidBecause(*expectedViolations)
        }
    }

    "Entries of maps are validated" {
        val toValidate = Dto()
        val expectedViolations = toValidate.map.map { (key, value) ->
            val expectedMessage = "$key:$value"
            key.failed(
                withMessage = expectedMessage,
                underPath = { -"map"["$key"] / "key" }
            )
            value.failed(
                withMessage = expectedMessage,
                underPath = { -"map"["$key"] / "value" }
            )
        }.toTypedArray()

        testValidation(
            of = toValidate,
            with = validation {
                subject::map require {
                    eachEntry(
                        keyValidation = { value -> +failWithMessage { "$subject:$value" } },
                        valueValidation = { key -> +failWithMessage { "$key:$subject" } },
                    )
                }
            }
        ) {
            result.shouldRepresentCompletedValidation()
                .shouldBeInvalidBecause(*expectedViolations)
        }
    }

    "Exception handling" - {
        val expectedException = RuntimeException("error")

        "Exceptions from validation rules are caught" {
            class ThrowingCheck(val ex: Throwable) :
                Check.Parameterless<Any, ThrowingCheck> by delegate({ throw ex })

            val spec = validation<Dto> {
                subject::simpleValue require {
                    +ThrowingCheck(expectedException).toValidationRule { "" }
                }
            }

            testValidation(Dto(), spec) {
                result.shouldBeFailure(expectedException)
            }
        }

        "Exceptions from nested blocks are caught" {
            val spec = validation<Dto> {
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

        "Exceptional operations can be recovered into validation blocks" {
            val expectedMessage = "exception msg"
            val spec = validation<Dto> {
                val failingResult = subject::simpleValue require {
                    subject::length require { throw IllegalStateException(expectedMessage) }
                }
                failingResult.recoverFrom<RuntimeException> { +pass }
            }

            testValidation(Dto(), spec) {
                result.shouldPass()
            }
        }
    }

    "Rules are processed conditionally" {
        val failFirst = "fail"
        val spec = validation<Dto> {
            val result = if (subject.simpleValue === failFirst) +failWithMessage { "1" } else +pass
            result.whenValid { +failWithMessage { "2" } }
        }

        testValidation(Dto(simpleValue = failFirst), spec) {
            result.shouldBeInvalidBecause(validated.failed(withMessage = "1"))
        }
        testValidation(Dto(), spec) {
            result.shouldBeInvalidBecause(validated.failed(withMessage = "2"))
        }
    }
})
