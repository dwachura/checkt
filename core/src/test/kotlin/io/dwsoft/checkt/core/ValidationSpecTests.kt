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

// TODO: get rid of subject references from DSL

class ValidationSpecTests : StringSpec({
    val root = ValidatedObject(
        simpleValue = Arb.string().next(),
        collection = Arb.list(Arb.double(), 3..3).next(),
        map = Arb.map(Arb.double(), Arb.string(), maxSize = 5).next(),
    )

    "Validation in root scope" {
        val validation = validate(root) {
            +alwaysFailWithMessage { "1" }
            subject.simpleValue requireTo alwaysFailWithMessage { "2" }
        }

        validation.shouldFailBecause(
            root.violated<AlwaysFailingCheck>(withMessage = "1"),
            root.simpleValue.violated<AlwaysFailingCheck>(withMessage = "2"),
        )
    }

    "Validation in nested scope" {
        val validation = validate(root) {
            subject.simpleValue.namedAs(!"simpleVal") requireTo { +alwaysFailWithMessage { "1" } }
            subject::collection requireTo { +alwaysFailWithMessage { "2" } }
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
            subject::collection {
                eachElement { idx ->
                    subject requireTo alwaysFailWithMessage { "$idx" }
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
            subject::map {
                eachEntry(
                    indexedUsingKeysTransformedBy = { !"$it" },
                    keyValidation = { +alwaysFailWithMessage { "$subject:$it" } },
                    valueValidation = { +alwaysFailWithMessage { "$it:$subject" } },
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
