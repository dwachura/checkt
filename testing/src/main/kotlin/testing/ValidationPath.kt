package io.dwsoft.checkt.testing

import io.dwsoft.checkt.core.NotBlankString
import io.dwsoft.checkt.core.ValidationPath
import io.dwsoft.checkt.core.ValidationPath.Segment
import io.dwsoft.checkt.core.ValidationPath.Segment.Name
import io.dwsoft.checkt.core.ValidationPath.Segment.NumericIndex
import io.dwsoft.checkt.core.asIndex
import io.dwsoft.checkt.core.asKey
import io.dwsoft.checkt.core.not
import io.dwsoft.checkt.core.plus
import io.dwsoft.checkt.testing.ValidationPathBuildingScope.LazyTailAppender
import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldContainInOrder

fun ValidationPath.shouldContainSegments(expected: List<Segment>) {
    val actualSegments = toList()
    "Validation path with segments $actualSegments, should contain passed values.".asClue {
        actualSegments.shouldContainInOrder(expected)
    }
}

fun ValidationPath.toList(): List<Segment> =
    (tail?.toList() ?: emptyList()) + head

fun ValidationPath.shouldContainSegments(vararg expected: Segment) =
    this.shouldContainSegments(expected.toList())

typealias ValidationPathBuilder = ValidationPathBuildingScope.() -> ValidationPath

fun path(builder: ValidationPathBuilder): ValidationPath =
    ValidationPathBuildingScope().builder()

class ValidationPathBuildingScope {
    val root: ValidationPath = ValidationPath()
    val `$` get() = root

    operator fun String.unaryMinus(): ValidationPath = startPath(Name(!this))

    operator fun LazyTailAppender.unaryMinus(): ValidationPath = toPath()

    operator fun ValidationPath.div(name: String): ValidationPath = this + !name

    operator fun ValidationPath.div(block: LazyTailAppender): ValidationPath =
        appendSegment(block)

    operator fun String.get(key: String): LazyTailAppender =
        appendLazily(!this).appendIndex((!key).asKey())

    operator fun String.get(index: NumericIndex): LazyTailAppender =
        appendLazily(!this).appendIndex(index)

    operator fun LazyTailAppender.get(index: NumericIndex): LazyTailAppender =
        this.appendIndex(index)

    operator fun LazyTailAppender.get(key: String): LazyTailAppender =
        this.appendIndex((!key).asKey())

    val Int.idx: NumericIndex
        get() = asIndex()

    private fun startPath(from: Segment): ValidationPath = root + from

    private fun LazyTailAppender.toPath(): ValidationPath = this(null)

    private fun ValidationPath.appendSegment(block: LazyTailAppender): ValidationPath = block(this)

    private fun appendLazily(name: NotBlankString): LazyTailAppender =
        LazyTailAppender {
            when (it) {
                null -> startPath(Name(name))
                else -> it + name
            }
        }

    private fun LazyTailAppender.appendIndex(index: Segment.Index): LazyTailAppender =
        LazyTailAppender { this(it) + index }

    fun interface LazyTailAppender : ((ValidationPath?) -> ValidationPath)
}
