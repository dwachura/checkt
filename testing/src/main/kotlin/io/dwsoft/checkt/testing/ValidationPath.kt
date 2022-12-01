package io.dwsoft.checkt.testing

import io.dwsoft.checkt.core.ValidationPath
import io.dwsoft.checkt.core.toList
import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldContainInOrder

fun ValidationPath.shouldContainSegments(expected: List<ValidationPath.Segment>) {
    val actualSegments = toList()
    "Validation path with segments $actualSegments, should contain passed values.".asClue {
        actualSegments.shouldContainInOrder(expected)
    }
}

fun ValidationPath.shouldContainSegments(vararg expected: ValidationPath.Segment) =
    this.shouldContainSegments(expected.toList())
