package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.ValidationPath.Segment
import io.dwsoft.checkt.core.ValidationPath.Segment.Empty
import io.dwsoft.checkt.core.ValidationPath.Segment.Index
import io.dwsoft.checkt.core.ValidationPath.Segment.Name
import io.dwsoft.checkt.testing.forAll
import io.dwsoft.checkt.testing.shouldContainSegments
import io.kotest.assertions.asClue
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
    "Appending segments..." - {
        "...to unnamed path" {
            val emptyPath = ValidationPath.unnamed
            forAll(pathSegmentKinds()) {
                when (this) {
                    is Empty -> (emptyPath + this).shouldContainSegments(Empty)
                    is Name -> (emptyPath + this).shouldContainSegments(this)
                    is Index -> {
                        shouldThrow<IllegalArgumentException> { emptyPath + this }
                            .message shouldContain "Empty path cannot be indexed"
                    }
                }
            }
        }

        "...to named path" {
            val expectedSegment = Name("path")
            val namedPath = ValidationPath.named(expectedSegment)
            forAll(pathSegmentKinds()) {
                when (this) {
                    is Empty -> (namedPath + this).shouldContainSegments(expectedSegment)
                    is Name, is Index ->
                        (namedPath + this).shouldContainSegments(expectedSegment, this)
                }
            }
        }

        "...to indexed path" {
            val expectedSegments = listOf(Name("path"), Index(0))
            val indexedPath = ValidationPath.named(expectedSegments[0] as Name) + expectedSegments[1]
            forAll(pathSegmentKinds()) {
                when (this) {
                    is Empty -> (indexedPath + this).shouldContainSegments(expectedSegments)
                    is Name, is Index ->
                        (indexedPath + this).shouldContainSegments(*expectedSegments.toTypedArray(), this)
                }
            }
        }
    }

    "Path is joined to string" {
        val prefix = ValidationPath.named(Name("seg1"))

        io.kotest.data.forAll(
            row(ValidationPath.unnamed, ""),
            row(prefix + Name("seg2"), "seg1.seg2"),
            row(prefix + Index("idx"), "seg1[idx]"),
            row(prefix + Index(0), "seg1[0]"),
            row(prefix + Index(0) + Index(1), "seg1[0][1]"),
            row(prefix + Index(0) + Name("seg2"), "seg1[0].seg2")
        ) { path, expected ->
            val joined = path.joinToString()

            joined shouldBe expected
        }
    }

    "Path is joined to string with empty root displayed separated by custom value" {
        val path = ValidationPath.unnamed + Name("seg1")

        val joined = path.joinToString(displayingEmptyRootAs = "$",) { s1, s2 -> "$s1 -> $s2" }

        joined shouldBe "$ -> seg1"
    }

    "Last token is returned" {
        val lastNamed = Name("last")
        val named = ValidationPath.named(Name("seg1")) + lastNamed

        io.kotest.data.forAll(
            row(ValidationPath.unnamed, ""),
            row(named, lastNamed.value),
            row(named + Index(0), "${lastNamed.value}${Index(0).value}"),
            row(named + Index(0) + Index(1), "${lastNamed.value}${Index(0).value}${Index(1).value}"),
        ) { path, expected ->
            val joined = path.lastToken()

            joined shouldBe expected
        }
    }

    "Validation path building" - {
        fun path(root: Segment, vararg segments: Segment) =
            segments.fold(
                if (root is Empty) ValidationPath.unnamed else ValidationPath.named(root as Name),
                ValidationPath::plus
            )
        val seg1 = Name("seg1")
        val seg2 = Name("seg2")
        val idx1 = Index(1)
        val idx2 = Index(2)
        val key1 = Index("key1")
        val key2 = Index("key2")

        forAll(
            row({ -"" }, path(Empty)),
            row({ -"" / "seg1" }, path(Empty, seg1)),
            row({ -"" / "seg1"[1.idx] }, path(Empty, seg1, idx1)),
            row({ -"" / "seg1"[1.idx][2.idx] }, path(Empty, seg1, idx1, idx2)),
            row({ -"" / "seg1"["key1"]["key2"] }, path(Empty, seg1, key1, key2)),
            row({ -"" / "seg1"[1.idx]["key1"][2.idx]["key2"] }, path(Empty, seg1, idx1, key1, idx2, key2)),
            row({ -"seg1" }, path(seg1)),
            row({ -"seg1" / "seg2" }, path(seg1, seg2)),
            row({ -"seg1"[1.idx] }, path(seg1, idx1)),
            row({ -"seg1"["key1"] }, path(seg1, key1)),
            row({ -"seg1" / "seg2"[1.idx] }, path(seg1, seg2, idx1)),
            row({ -"seg1" / "seg2"[1.idx][2.idx] }, path(seg1, seg2, idx1, idx2)),
            row({ -"seg1" / "seg2"["key1"]["key2"] }, path(seg1, seg2, key1, key2)),
            row({ -"seg1" / "seg2"[1.idx]["key1"][2.idx]["key2"] }, path(seg1, seg2, idx1, key1, idx2, key2)),
        ) { buildPath: ValidationPathBuilder, expectedPath: ValidationPath ->
            val readableExpectedPath = expectedPath.joinToString(displayingEmptyRootAs = "$")
            "Should build '$readableExpectedPath'" {
                val pathBuilt = validationPath(buildPath)
                val readablePathBuilt = pathBuilt.joinToString(displayingEmptyRootAs = "$")
                "'$readablePathBuilt' should be the same as '$readableExpectedPath'".asClue {
                    pathBuilt shouldBe expectedPath
                }
            }
        }
    }
})

private fun pathSegmentKinds(): Exhaustive<Segment> =
    Segment::class.sealedSubclasses
        .map {
            when (it) {
                Empty::class -> Empty
                Name::class -> Name("Segment")
                Index::class -> Index(123)
                else -> throw IllegalStateException("All segment kinds must be tested")
            }
        }.toList()
        .let { Exhaustive.of(*it.toTypedArray()) }

class SegmentTests : FreeSpec({
    "${Name::class.simpleName} creation" {
        forAll(strings()) {
            when {
                isEmpty() -> {
                    shouldThrow<IllegalArgumentException> { Name(this) }
                        .message shouldContain "Named segment cannot be blank"
                }
                else -> shouldNotThrowAny { Name(this) }
            }
        }
    }

    "${Index::class.simpleName} creation" - {
        "From string" {
            forAll(strings()) {
                when {
                    this.isEmpty() -> {
                        shouldThrow<IllegalArgumentException> { Index(this) }
                            .message shouldContain "Index of path segment cannot be blank"
                    }
                    else -> shouldNotThrowAny { Index(this) }
                }
            }
        }

        "From number" {
            forAll(ints()) {
                when {
                    this < 0 -> {
                        shouldThrow<IllegalArgumentException> { Index(this) }
                            .message shouldContain "Numeric index cannot be negative"
                    }
                    else -> shouldNotThrowAny { Index(this) }
                }
            }
        }
    }
})

private fun strings(): Exhaustive<String> =
    Exhaustive.of(
        "",
        Arb.string(minSize = 1).next()
    )

private fun ints(): Exhaustive<Int> =
    Exhaustive.of(
        Arb.negativeInt().next(),
        0,
        Arb.positiveInt().next()
    )