package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.ValidationPath.Segment

/**
 * Non-blank string representation.
 *
 * @constructor throws [IllegalArgumentException] when [value] is blank.
 */
@JvmInline
value class NonBlankString(val value: String) {
    init {
        require(value.isNotBlank()) { "Value cannot be blank" }
    }

    override fun toString(): String = value
}

/**
 * Utility extension over [String] to create [NonBlankString] in a concise manner.
 * Alias of [NonBlankString] constructor.
 */
operator fun String.not(): NonBlankString = NonBlankString(this)

sealed interface ValidationPath {
    val head: Segment
    val tail: ValidationPath?

    sealed interface Segment {
        val value: String

        object Empty : Segment {
            override val value: String = ""
        }

        data class Name(val rawValue: NonBlankString) : Segment {
            override val value: String
                get() = rawValue.value
        }

        data class Index(val rawValue: NonBlankString) : Segment {
            constructor(number: Int) : this(!"$number") {
                require(number >= 0) { "Numeric index cannot be negative" }
            }

            override val value: String = "[$rawValue]"
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
