package io.dwsoft.checkt.core.check

import io.kotest.core.spec.style.FreeSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table

class ContainmentChecksTests : FreeSpec({
    "${ContainsAny::class.simpleName}" - {
        val expectedValues = listOf(2, 3, 4)
        "collection containing any of $expectedValues passes check" {
            forAll(
                table(
                    headers("collection"),
                    row(listOf(3)),
                    row(listOf(2, 3)),
                    row((2..5).toList()),
                    row((1..5).toList())
                )
            ) { collection ->
                val check = ContainsAny(*expectedValues.toTypedArray())

                collection shouldPass check
            }
        }

        "collection not containing any of $expectedValues violates check" {
            forAll(
                table(
                    headers("collection"),
                    row(emptyList()),
                    row(listOf(1, 5, 6))
                )
            ) { collection ->
                val check = ContainsAny(*expectedValues.toTypedArray())

                collection shouldNotPass  check
            }


        }
    }

    "${ContainsAll::class.simpleName}" - {
        val expectedValues = listOf(3, 3, 4, 5)
        "collection containing all of $expectedValues passes check" {
            forAll(
                table(
                    headers("collection"),
                    row(listOf(3, 3, 4, 5)),
                    row(listOf(3, 4, 5) + expectedValues)
                )
            ) { collection ->
                val check = ContainsAll(*expectedValues.toTypedArray())

                collection shouldPass check
            }
        }

        "collection not containing all of $expectedValues violates check" {
            forAll(
                table(
                    headers("collection"),
                    row(emptyList()),
                    row(listOf(3, 4)),
                    row(setOf(*expectedValues.toTypedArray()))
                )
            ) { collection ->
                val check = ContainsAll(*expectedValues.toTypedArray())

                collection shouldNotPass  check
            }
        }
    }
})
