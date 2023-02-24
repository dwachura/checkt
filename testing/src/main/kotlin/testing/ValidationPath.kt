package io.dwsoft.checkt.testing

import io.dwsoft.checkt.core.NotBlankString
import io.dwsoft.checkt.core.ValidationPath
import io.dwsoft.checkt.core.ValidationPath.Element
import io.dwsoft.checkt.core.ValidationPath.Element.Segment
import io.dwsoft.checkt.core.ValidationPath.Element.NumericIndex
import io.dwsoft.checkt.core.asIndex
import io.dwsoft.checkt.core.asKey
import io.dwsoft.checkt.core.not
import io.dwsoft.checkt.core.plus
import io.dwsoft.checkt.testing.ValidationPathBuildingScope.LazyTailAppender
import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldContainInOrder

fun ValidationPath.shouldContainElements(expected: List<Element>) {
    val actualElements = toList()
    "Validation path with elements $actualElements, should contain passed values.".asClue {
        actualElements.shouldContainInOrder(expected)
    }
}

fun ValidationPath.toList(): List<Element> =
    (tail?.toList() ?: emptyList()) + head

fun ValidationPath.shouldContainElements(vararg expected: Element) =
    shouldContainElements(expected.toList())

typealias ValidationPathBuilder = ValidationPathBuildingScope.() -> ValidationPath

fun validationPath(builder: ValidationPathBuilder): ValidationPath =
    ValidationPathBuildingScope().builder()

class ValidationPathBuildingScope {
    val root: ValidationPath = ValidationPath()
    val `$` get() = root

    operator fun String.unaryMinus(): ValidationPath = startPath(Segment(!this))

    operator fun LazyTailAppender.unaryMinus(): ValidationPath = toPath()

    operator fun ValidationPath.div(name: String): ValidationPath = this + !name

    operator fun ValidationPath.div(block: LazyTailAppender): ValidationPath =
        appendElement(block)

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

    private fun startPath(from: Element): ValidationPath = root + from

    private fun LazyTailAppender.toPath(): ValidationPath = this(null)

    private fun ValidationPath.appendElement(block: LazyTailAppender): ValidationPath = block(this)

    private fun appendLazily(name: NotBlankString): LazyTailAppender =
        LazyTailAppender {
            when (it) {
                null -> startPath(Segment(name))
                else -> it + name
            }
        }

    private fun LazyTailAppender.appendIndex(index: Element.Index): LazyTailAppender =
        LazyTailAppender { this(it) + index }

    fun interface LazyTailAppender : ((ValidationPath?) -> ValidationPath)
}
