package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.ValidationPath.Segment
import io.dwsoft.checkt.core.ValidationPathBuildingScope.LazyTailAppender

sealed interface ValidationPath {
    val head: Segment
    val tail: ValidationPath?

    sealed interface Segment {
        val value: String

        object Empty : Segment {
            override val value: String = ""
        }

        data class Name(override val value: String) : Segment {
            init {
                require(value.isNotBlank()) { "Named segment cannot be blank" }
            }
        }

        data class Index(val rawValue: String) : Segment {
            constructor(number: Int) : this(number.toString()) {
                require(number >= 0) { "Numeric index cannot be negative" }
            }

            override val value: String = "[$rawValue]"

            init {
                require(rawValue.isNotBlank()) { "Index of path segment cannot be blank" }
            }
        }
    }

    companion object
}

val ValidationPath.Companion.unnamed: ValidationPath
    get() = ValidationPathImpl.empty

fun ValidationPath.Companion.named(name: Segment.Name): ValidationPath = ValidationPath(name)

operator fun ValidationPath.plus(segment: Segment): ValidationPath =
    when (segment) {
        Segment.Empty -> this
        is Segment.Name -> ValidationPath(segment, this)
        is Segment.Index -> {
            require(this != ValidationPathImpl.empty) { "Empty path cannot be indexed" }
            ValidationPath(segment, this)
        }
    }

fun ValidationPath.joinToString(
    displayingEmptyRootAs: String = "",
    joiner: (String, String) -> String = { s1, s2 -> "$s1.$s2" }
): String =
    tail?.let {
        val tailDisplay = it.joinToString(displayingEmptyRootAs, joiner)
        val headDisplay = head.value
        when {
            tailDisplay.isEmpty() -> headDisplay
            head is Segment.Empty -> tailDisplay
            head is Segment.Index -> "$tailDisplay$headDisplay"
            else -> joiner(tailDisplay, headDisplay)
        }
    } ?: if (head is Segment.Empty) displayingEmptyRootAs else head.value

fun ValidationPath.lastToken(): String =
    when (head) {
        is Segment.Empty, is Segment.Name -> head.value
        else -> {
            tail?.let {
                it.lastToken() + head.value
            } ?: head.value
        }
    }

fun ValidationPath.toList(): List<Segment> =
    (tail?.toList() ?: emptyList()) + head

private fun ValidationPath(head: Segment, tail: ValidationPath? = null): ValidationPath =
    ValidationPathImpl(head, tail)

private data class ValidationPathImpl(
    override val head: Segment,
    override val tail: ValidationPath?,
) : ValidationPath {
    init {
        if (tail == null) require(head !is Segment.Index) { "Validation path cannot start with index" }
    }

    companion object {
        val empty = ValidationPath(Segment.Empty)
    }
}

typealias ValidationPathBuilder = ValidationPathBuildingScope.() -> ValidationPath

fun validationPath(block: ValidationPathBuilder): ValidationPath = ValidationPathBuildingScope().block()

// TODO: kdocks?
class ValidationPathBuildingScope {
    operator fun String.unaryMinus(): ValidationPath = startPath(from = this)

    operator fun LazyTailAppender.unaryMinus(): ValidationPath = toPath()

    operator fun ValidationPath?.div(name: String): ValidationPath = appendSegment(name)

    operator fun ValidationPath?.div(block: LazyTailAppender): ValidationPath = appendSegment(block)

    operator fun String.get(key: String): LazyTailAppender =
        appendLazily(this).appendIndex(Segment.Index(key))

    operator fun String.get(index: NumericIndex): LazyTailAppender =
        appendLazily(this).appendIndex(Segment.Index(index.idx))

    operator fun LazyTailAppender.get(index: NumericIndex): LazyTailAppender =
        this.appendIndex(Segment.Index(index.idx))

    operator fun LazyTailAppender.get(key: String): LazyTailAppender =
        this.appendIndex(Segment.Index(key))

    val Int.idx: NumericIndex
        get() = NumericIndex(this)

    private fun startPath(from: String): ValidationPath =
        from.toSegment().let {
            when (it) {
                is Segment.Empty -> ValidationPath.unnamed
                is Segment.Name -> ValidationPath.named(it)
                else -> throw RuntimeException("Cannot happen")
            }
        }

    private fun String.toSegment(): Segment =
        when {
            isBlank() -> Segment.Empty
            else -> Segment.Name(this)
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

    private fun LazyTailAppender.appendIndex(index: Segment.Index): LazyTailAppender =
        LazyTailAppender { this(it) + index }

    @JvmInline
    value class NumericIndex(val idx: Int)

    fun interface LazyTailAppender : ((ValidationPath?) -> ValidationPath)
}
