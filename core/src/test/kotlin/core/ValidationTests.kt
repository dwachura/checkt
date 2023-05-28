package io.dwsoft.checkt.core

import io.dwsoft.checkt.testing.failWithMessage
import io.dwsoft.checkt.testing.failed
import io.dwsoft.checkt.testing.pass
import io.dwsoft.checkt.testing.shouldBeInvalid
import io.dwsoft.checkt.testing.shouldBeInvalidBecause
import io.dwsoft.checkt.testing.shouldBeInvalidExactlyBecause
import io.dwsoft.checkt.testing.shouldBeValid
import io.dwsoft.checkt.testing.shouldFailWith
import io.dwsoft.checkt.testing.testValidation
import io.dwsoft.checkt.testing.validationPath
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.mockk

class ValidationTests : FreeSpec({
    data class Dto(
        val simpleValue: String = Arb.string().next(),
        val collection: Collection<Number> = emptyList(),
        val map: Map<Double, String> = emptyMap(),
        val nullableValue: Any? = null,
    )

    "Resulting and sub-statuses are returned correctly" {
        testValidation(
            of = Dto(),
            with = validation {
                (+pass).shouldBeValid()
                (+failWithMessage { "1" }).shouldBeInvalid(withViolationsCountEqualTo = 1)
                the.simpleValue {
                    +failWithMessage { "2" }
                    +failWithMessage { "3" }
                }.shouldBeInvalid(withViolationsCountEqualTo = 2)
                require(the::collection) {
                    +failWithMessage { "4" }
                }.shouldBeInvalid(withViolationsCountEqualTo = 1)
                require(the.map.namedAs(!"customName")) {
                    +failWithMessage { "5" }
                }.shouldBeInvalid(withViolationsCountEqualTo = 1)
            }
        ) {
            result.shouldBeInvalidExactlyBecause(
                validated.failed { withMessage("1") },
                validated.simpleValue.failed { withMessage("2") },
                validated.simpleValue.failed { withMessage("3") },
                validated.collection.failed { withMessage("4"); underPath { -"collection" } },
                validated.map.failed { withMessage("5"); underPath { -"customName" } },
            )
        }
    }

    "Iterable's elements are validated" {
        val toValidate = Dto(collection = Arb.list(Arb.double(), 2..3).next())
        val expectedViolations = toValidate.collection.mapIndexed { idx, elem ->
            elem.failed {
                underPath { -"collection"[idx.idx] }
                withMessage("$idx")
            }
        }.toTypedArray()

        testValidation(
            of = toValidate,
            with = validation {
                require(the::collection) {
                    eachElement { idx -> +failWithMessage { "$idx" } }
                }
            }
        ) {
            result.shouldBeInvalidExactlyBecause(*expectedViolations)
        }
    }

    "Map's entries are validated" {
        val toValidate = Dto(
            map = Arb.map(Arb.double(), Arb.string(), minSize = 2, maxSize = 3).next()
        )
        val expectedViolations = toValidate.map.flatMap { (key, value) ->
            val msg = "$key:$value"
            val pathPrefix = validationPath { -"map"["$key"] }
            listOf(
                key.failed { withMessage(msg); underPath { pathPrefix / "key" } },
                value.failed { withMessage(msg); underPath { pathPrefix / "value" } }
            )
        }.toTypedArray()

        testValidation(
            of = toValidate,
            with = validation {
                require(the::map) {
                    eachEntry(
                        keyValidation = { value -> +failWithMessage { "$subject:$value" } },
                        valueValidation = { key -> +failWithMessage { "$key:$subject" } },
                    )
                }
            }
        ) {
            result.shouldBeInvalidExactlyBecause(*expectedViolations)
        }
    }

    "Naming duplication errors are not caught" {
        shouldThrow<ValidationScope.NamingUniquenessException> {
            testValidation(
                of = Dto(),
                with = validation {
                    require(the::simpleValue) { +pass }
                    require(the.collection.namedAs(!"simpleValue")) { +pass }
                }
            ) {}
        }
    }

    "First exceptional operation fails validation" {
        val expectedException = RuntimeException("error")
        val spec = validation<Dto> {
            require(the::simpleValue) { +pass }
            require(the::collection) { throw expectedException }
            require(the::map) { throw RuntimeException() }
        }

        testValidation(Dto(), spec) {
            result.shouldFailWith(expectedException)
        }
    }

    "Rules can be executed conditionally depending on..." - {
        "...the other rule's result" {
            val failFirst = "fail"
            val spec = validation<Dto> {
                val status = if (the.simpleValue === failFirst) +failWithMessage { "1" } else +pass
                status.whenValid { +failWithMessage { "2" } }
            }

            testValidation(Dto(simpleValue = failFirst), spec) {
                result.shouldBeInvalidBecause(validated.failed { withMessage("1") })
            }
            testValidation(Dto(), spec) {
                result.shouldBeInvalidBecause(validated.failed { withMessage("2") })
            }
        }

        "...whether a value is not null" {
            val spec = validation<Dto> {
                requireUnlessNull(the::nullableValue) { +failWithMessage { "1" } }
            }

            testValidation(Dto(nullableValue = null), spec) { result.shouldBeValid() }
            testValidation(Dto(nullableValue = Any()), spec) {
                result.shouldBeInvalidBecause(
                    validated.nullableValue.failed{ underPath { -"nullableValue" }; withMessage("1") }
                )
            }
        }
    }

    "Exceptions handling" - {
        val expectedException = RuntimeException("error")

        "Exceptions thrown by validation rules are caught" {
            class ThrowingCheck(val ex: Throwable) : Check<Any> by Check({ throw ex })

            val spec = validation<Dto> {
                require(the::simpleValue) {
                    +ThrowingCheck(expectedException).toValidationRule(mockk()) { "" }
                }
            }

            testValidation(Dto(), spec) {
                result.shouldFailWith(expectedException)
            }
        }

        "Exceptions from nested blocks are caught" - {
            "Simple nesting" {
                val spec = validation<Dto> {
                    require(the::simpleValue) {
                        require(the::length) {
                            require(subject.namedAs(!"deeper")) { throw expectedException }
                        }
                    }
                }

                testValidation(Dto(), spec) {
                    result.shouldFailWith(expectedException)
                }
            }

            "Iterated nesting" {
                val spec = validation<Dto> {
                    require(the::collection) {
                        eachElement { idx -> throw RuntimeException("$idx") }
                    }
                }

                testValidation(
                    Dto(collection = Arb.list(Arb.double(), 2..3).next()),
                    spec
                ) {
                    result.shouldFailWith(RuntimeException("0"))
                }
            }

            "Key/value nesting" - {
                val validated = Dto(
                    map = Arb.map(Arb.double(), Arb.string(), minSize = 2, maxSize = 3).next()
                )

                "Key validation error" {
                    val spec = validation<Dto> {
                        require(the::map) {
                            eachEntry(
                                keyValidation = { throw RuntimeException(it) },
                                valueValidation = { +pass }
                            )
                        }
                    }

                    testValidation(validated, spec) {
                        result.shouldFailWith(RuntimeException(validated.map.values.first()))
                    }
                }

                "Value validation error" {
                    val spec = validation<Dto> {
                        require(the::map) {
                            eachEntry { throw RuntimeException(subject) }
                        }
                    }

                    testValidation(validated, spec) {
                        result.shouldFailWith(RuntimeException(validated.map.values.first()))
                    }
                }
            }
        }

        "Exceptional operations can be recovered into validation blocks" {
            val expectedMessage = "exception msg"
            val spec = validation<Dto> {
                the.simpleValue {
                    catching {
                        require(the::length) { throw IllegalStateException(expectedMessage) }
                    }.recover(
                        from<IllegalStateException> { +pass }
                    ).whenValid {
                        +failWithMessage { "1" }
                    }
                }
            }

            testValidation(Dto(), spec) {
                result.shouldBeInvalidBecause(
                    validated.simpleValue.failed { withMessage("1") }
                )
            }
        }
    }
})
