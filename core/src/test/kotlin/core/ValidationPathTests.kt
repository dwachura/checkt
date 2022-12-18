package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.ValidationPath.Segment.NumericIndex
import io.dwsoft.checkt.testing.forAll
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.arbitrary.negativeInt
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.positiveInt
import io.kotest.property.arbitrary.string
import io.kotest.property.exhaustive.of

class ValidationPathTests : FreeSpec({
    val root = Checkt.Options.ValidationPath.rootSegmentValue

    "Path is joined to string..." - {
        "...using default joiner" {
            forAll(
                row(ValidationPath() + !"seg1", "$root.seg1"),
                row(ValidationPath() + (!"key").asKey(), "$root[key]"),
                row(ValidationPath() + 0.asIndex(), "$root[0]"),
                row(ValidationPath(!"seg1") + (!"key").asKey(), "$root.seg1[key]"),
                row(ValidationPath(!"seg1") + 0.asIndex(), "$root.seg1[0]"),
                row(ValidationPath(!"seg1") + 0.asIndex() + 1.asIndex(), "$root.seg1[0][1]"),
                row(ValidationPath(!"seg1") + 0.asIndex() + !"seg2", "$root.seg1[0].seg2")
            ) { path: ValidationPath, expected: String ->
                val joined = path.joinToString()

                joined shouldBe expected
            }
        }

        "...using custom joiner" {
            val separator = Arb.string(maxSize = 5).next()
            val path = ValidationPath() + !"seg1" + 0.asIndex() + (!"key").asKey()

            val joined = path.joinToString { s1: String, s2: String -> "$s1 | $separator | $s2" }

            joined shouldBe "$root | $separator | seg1[0][key]"
        }
    }

    "Last sub-path is returned" {
        val start = ValidationPath(!"s1")

        forAll(
            row(start + !"s2" + !"s3", "s3"),
            row(start + !"s2" + 0.asIndex(), "s2[0]"),
            row(start + !"s2" + 0.asIndex() + 1.asIndex(), "s2[0][1]"),
            row(start + !"s2" + (!"k1").asKey(), "s2[k1]"),
            row(start + !"s2" + (!"k1").asKey() + (!"k2").asKey(), "s2[k1][k2]"),
            row(start + !"s2" + 0.asIndex() + (!"k1").asKey(), "s2[0][k1]"),
        ) { path: ValidationPath, expected: String ->
            val joined = path.lastSubPath()

            joined shouldBe expected
        }
    }
})

class SegmentTests : FreeSpec({
    "${NumericIndex::class.simpleName} creation" {
        forAll(ints()) {
            when {
                this < 0 -> {
                    shouldThrow<IllegalArgumentException> {
                        NumericIndex(this)
                    }.message shouldContain "Numeric index cannot be negative"
                }
                else -> shouldNotThrowAny { NumericIndex(this) }
            }
        }
    }
})

private fun ints(): Exhaustive<Int> =
    Exhaustive.of(
        Arb.negativeInt().next(),
        0,
        Arb.positiveInt().next()
    )
