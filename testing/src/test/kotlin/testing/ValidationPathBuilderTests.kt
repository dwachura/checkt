package io.dwsoft.checkt.testing

import io.dwsoft.checkt.core.NotBlankString
import io.dwsoft.checkt.core.ValidationPath
import io.dwsoft.checkt.core.ValidationPath.Element.Key
import io.dwsoft.checkt.core.ValidationPath.Element.NumericIndex
import io.dwsoft.checkt.core.joinToString
import io.dwsoft.checkt.core.not
import io.dwsoft.checkt.core.plus
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.FreeSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe

class ValidationPathBuilderTests : FreeSpec({
    "Validation path builder..." - {
        fun path(name: NotBlankString): ValidationPath = ValidationPath(name)
        val seg1 = !"seg1"
        val seg2 = !"seg2"
        val idx1 = NumericIndex(1)
        val idx2 = NumericIndex(2)
        val key1 = Key(!"key1")
        val key2 = Key(!"key2")


        forAll(
            row({ -"seg1" }, path(seg1)),
            row({ -"seg1" / "seg2" }, path(seg1) + seg2),
            row({ -"seg1"[1.idx] }, path(seg1) + idx1),
            row({ -"seg1"["key1"] }, path(seg1) + key1),
            row({ -"seg1" / "seg2"[1.idx] }, path(seg1) + seg2 + idx1),
            row({ -"seg1" / "seg2"[1.idx][2.idx] }, path(seg1) + seg2 + idx1 + idx2),
            row({ -"seg1" / "seg2"["key1"]["key2"] }, path(seg1) + seg2 + key1 + key2),
            row({ -"seg1" / "seg2"[1.idx]["key1"][2.idx]["key2"] }, path(seg1) + seg2 + idx1 + key1 + idx2 + key2),
        ) { pathBuilder: ValidationPathBuilder, expectedPath: ValidationPath ->
            val actualPath = validationPath(pathBuilder)
            val readableExpectedPath = expectedPath.joinToString()
            "...should build '$readableExpectedPath' path" {
                val readablePathBuilt = actualPath.joinToString()
                "'$readablePathBuilt' should be the same as '$readableExpectedPath'".asClue {
                    actualPath shouldBe expectedPath
                }
            }
        }
    }
})
