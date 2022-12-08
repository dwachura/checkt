package io.dwsoft.checkt.testing

import io.dwsoft.checkt.core.ValidationPath
import io.dwsoft.checkt.core.ValidationPath.Segment
import io.dwsoft.checkt.core.ValidationPath.Segment.Empty
import io.dwsoft.checkt.core.ValidationPath.Segment.Index
import io.dwsoft.checkt.core.ValidationPath.Segment.Name
import io.dwsoft.checkt.core.joinToString
import io.dwsoft.checkt.core.named
import io.dwsoft.checkt.core.not
import io.dwsoft.checkt.core.plus
import io.dwsoft.checkt.core.unnamed
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.FreeSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe

class ValidationPathBuilderTests : FreeSpec({
    "Validation path building" - {
        val seg1 = Name(!"seg1")
        val seg2 = Name(!"seg2")
        val idx1 = Index(1)
        val idx2 = Index(2)
        val key1 = Index(!"key1")
        val key2 = Index(!"key2")

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

private fun path(root: Segment, vararg segments: Segment) =
    segments.fold(
        if (root is Empty) ValidationPath.unnamed else ValidationPath.named(root as Name),
        ValidationPath::plus
    )
