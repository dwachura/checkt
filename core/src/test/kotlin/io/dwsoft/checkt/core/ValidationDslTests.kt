package io.dwsoft.checkt.core

import io.dwsoft.checkt.testing.AlwaysFailingCheck
import io.dwsoft.checkt.testing.alwaysFailWithMessage
import io.dwsoft.checkt.testing.shouldFailBecause
import io.dwsoft.checkt.testing.violated
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string

class ValidationDslTests : StringSpec({
    val root = ValidatedObject(
        simpleValue = Arb.string().next(),
        collection = Arb.list(Arb.double(), 3..3).next(),
        map = Arb.map(Arb.double(), Arb.string(), maxSize = 5).next(),
    )

    "Validation in root scope" {
        val validation = validate(root) {
            +alwaysFailWithMessage { "1" }
            simpleValue must alwaysFailWithMessage { "2" }
        }

        validation.shouldFailBecause(
            root.violated<AlwaysFailingCheck>(withMessage = "1"),
            root.simpleValue.violated<AlwaysFailingCheck>(withMessage = "2"),
        )
    }

    "Validation in nested scope" {
        val validation = validate(root) {
            simpleValue.namedAs(!"simpleVal") requireTo { +alwaysFailWithMessage { "1" } }
            ::collection requireTo { +alwaysFailWithMessage { "2" } }
        }

        validation.shouldFailBecause(
            root.simpleValue.violated<AlwaysFailingCheck>(
                withMessage = "1",
                underPath = { -"" / "simpleVal" }
            ),
            root.collection.violated<AlwaysFailingCheck>(
                withMessage = "2",
                underPath = { -"" / "collection" }
            ),
        )
    }

    "Elements of iterable properties can be validated" {
        val validation = validate(root) {
            ::collection {
                eachElement { idx ->
                    this must alwaysFailWithMessage { "$idx" }
                }
            }
        }
        val expectedViolations = root.collection.mapIndexed { idx, elem ->
            elem.violated<AlwaysFailingCheck>(
                underPath = { -"" / "collection"[idx.idx] },
                withMessage = "$idx"
            )
        }.toTypedArray()

        validation.shouldFailBecause(*expectedViolations)
    }

    "Entries of maps can be validated" {
        val validation = validate(root) {
            ::map {
                eachEntry(
                    indexedUsingKeysTransformedBy = { !"$it" },
                    keyValidation = { +alwaysFailWithMessage { "${this@eachEntry}:$it" } },
                    valueValidation = { +alwaysFailWithMessage { "$it:${this@eachEntry}" } },
                )
            }
        }
        val expectedViolations = root.map.map { (key, value) ->
            key.violated<AlwaysFailingCheck>(
                withMessage = "$key:$value",
                underPath = { -"" / "map"["$key"] / "key" }
            )
            value.violated<AlwaysFailingCheck>(
                withMessage = "$key:$value",
                underPath = { -"" / "map"["$key"] / "value" }
            )
        }.toTypedArray()

        validation.shouldFailBecause(*expectedViolations)
    }
})

private data class ValidatedObject(
    val simpleValue: String,
    val collection: Collection<Number>,
    val map: Map<Double, String>,
)
