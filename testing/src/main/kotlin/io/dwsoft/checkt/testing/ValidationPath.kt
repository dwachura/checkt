package io.dwsoft.checkt.testing

import io.dwsoft.checkt.core.ValidationPath
import io.dwsoft.checkt.core.named
import io.dwsoft.checkt.core.not
import io.dwsoft.checkt.core.plus
import io.dwsoft.checkt.core.toList
import io.dwsoft.checkt.core.unnamed
import io.dwsoft.checkt.testing.ValidationPathBuildingScope.LazyTailAppender
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

typealias ValidationPathBuilder = ValidationPathBuildingScope.() -> ValidationPath

fun validationPath(block: ValidationPathBuilder): ValidationPath = ValidationPathBuildingScope().block()

class ValidationPathBuildingScope {
    operator fun String.unaryMinus(): ValidationPath = startPath(from = this)

    operator fun LazyTailAppender.unaryMinus(): ValidationPath = toPath()

    operator fun ValidationPath?.div(name: String): ValidationPath = appendSegment(name)

    operator fun ValidationPath?.div(block: LazyTailAppender): ValidationPath = appendSegment(block)

    operator fun String.get(key: String): LazyTailAppender =
        appendLazily(this).appendIndex(ValidationPath.Segment.Index(!key))

    operator fun String.get(index: NumericIndex): LazyTailAppender =
        appendLazily(this).appendIndex(ValidationPath.Segment.Index(index.idx))

    operator fun LazyTailAppender.get(index: NumericIndex): LazyTailAppender =
        this.appendIndex(ValidationPath.Segment.Index(index.idx))

    operator fun LazyTailAppender.get(key: String): LazyTailAppender =
        this.appendIndex(ValidationPath.Segment.Index(!key))

    val Int.idx: NumericIndex
        get() = NumericIndex(this)

    private fun startPath(from: String): ValidationPath =
        from.toSegment().let {
            when (it) {
                is ValidationPath.Segment.Empty -> ValidationPath.unnamed
                is ValidationPath.Segment.Name -> ValidationPath.named(it)
                else -> throw RuntimeException("Cannot happen")
            }
        }

    private fun String.toSegment(): ValidationPath.Segment =
        when {
            isBlank() -> ValidationPath.Segment.Empty
            else -> ValidationPath.Segment.Name(!this)
        }

    private fun LazyTailAppender.toPath(): ValidationPath = this(null)

    private fun ValidationPath?.appendSegment(name: String): ValidationPath =
        when (this) {
            null -> startPath(from = name)
            else -> this + name.toSegment()
        }

    private fun ValidationPath?.appendSegment(block: LazyTailAppender): ValidationPath =
        block(this)

    private fun appendLazily(name: String): LazyTailAppender =
        LazyTailAppender { it.appendSegment(name) }

    private fun LazyTailAppender.appendIndex(index: ValidationPath.Segment.Index): LazyTailAppender =
        LazyTailAppender { this(it) + index }

    @JvmInline
    value class NumericIndex(val idx: Int)

    fun interface LazyTailAppender : ((ValidationPath?) -> ValidationPath)
}
