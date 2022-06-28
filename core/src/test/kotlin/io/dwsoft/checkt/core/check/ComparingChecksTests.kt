package io.dwsoft.checkt.core.check

import io.kotest.core.spec.style.FreeSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table

class ComparingChecksTests : FreeSpec({
    "${LessThan::class.simpleName} check" - {
        "less value passes check" {
            5 shouldPass LessThan(10)
        }

        "non-less value violates check" {
            forAll(
                table(
                    headers("non-less value", "max"),
                    row(15, 10),
                    row(10, 10)
                )
            ) { nonLessValue, max ->
                nonLessValue shouldNotPass LessThan(max)
            }
        }
    }

    "${LessThanOrEqual::class.simpleName} check" - {
        "non-greater value passes check" {
            forAll(
                table(
                    headers("non-greater value", "max"),
                    row(10, 10),
                    row(5, 10)
                )
            ) { nonGreaterValue, max ->
                nonGreaterValue shouldPass LessThanOrEqual(max)
            }
        }

        "greater value violates check" {
            15 shouldNotPass LessThanOrEqual(10)
        }
    }

    "${GreaterThan::class.simpleName} check" - {
        "greater value passes check" {
            15 shouldPass GreaterThan(10)
        }

        "non-greater value violates check" {
            forAll(
                table(
                    headers("non-greater value", "min"),
                    row(5, 10),
                    row(10, 10)
                )
            ) { nonGreaterValue, min ->
                nonGreaterValue shouldNotPass  GreaterThan(min)
            }
        }
    }

    "${GreaterThanOrEqual::class.simpleName} check" - {
        "non-less value passes check" {
            forAll(
                table(
                    headers("non-less value", "min"),
                    row(10, 10),
                    row(15, 10)
                )
            ) { nonLessValue, min ->
                nonLessValue shouldPass GreaterThanOrEqual(min)
            }
        }

        "less value violates check" {
            5 shouldNotPass GreaterThanOrEqual(10)
        }
    }
})
