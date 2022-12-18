package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.ValidationPath.Segment
import io.dwsoft.checkt.core.ValidationPath.Segment.Index
import io.dwsoft.checkt.core.ValidationPath.Segment.Key
import io.dwsoft.checkt.core.ValidationPath.Segment.Name
import io.dwsoft.checkt.core.ValidationPath.Segment.NumericIndex

/**
 * Non-blank string representation.
 *
 * @constructor throws [IllegalArgumentException] when [value] is
 * blank.
 */
@JvmInline
value class NotBlankString(val value: String) {
    init {
        require(value.isNotBlank()) { "Value cannot be blank" }
    }

    override fun toString(): String = value
}

/**
 * Utility extension over [String] to create [NotBlankString] in
 * a concise manner.
 *
 * Alias of [NotBlankString] constructor.
 */
operator fun String.not(): NotBlankString = NotBlankString(this)

sealed interface ValidationPath {
    val head: Segment
    val tail: ValidationPath?

    sealed interface Segment {
        val value: String

        data class Name(val rawValue: NotBlankString) : Segment {
            override val value: String
                get() = rawValue.value
        }

        sealed interface Index : Segment

        data class NumericIndex(val rawValue: Int) : Index {
            init {
                require(rawValue >= 0) {
                    "Numeric index cannot be negative"
                }
            }

            override val value: String = "[$rawValue]"
        }

        data class Key(val rawValue: NotBlankString) : Index {
            override val value: String = "[$rawValue]"
        }
    }

    companion object {
        private val rootPath: ValidationPath =
            ValidationPathInternal(Root, null)

        operator fun invoke() = rootPath

        operator fun invoke(head: Segment) =
            rootPath + head

        operator fun invoke(name: NotBlankString) =
            rootPath + Name(name)
    }
}

private object Root : Segment {
    override val value: String
        get() = Checkt.Options.ValidationPath.rootSegmentValue
}

private data class ValidationPathInternal(
    override val head: Segment,
    override val tail: ValidationPath?
) : ValidationPath

operator fun ValidationPath.plus(name: Segment): ValidationPath =
    ValidationPathInternal(name, this)

operator fun ValidationPath.plus(name: NotBlankString) = this + Name(name)

fun Int.asIndex(): NumericIndex = NumericIndex(this)

fun NotBlankString.asKey(): Key = Key(this)

fun ValidationPath.joinToString(
    joiner: (String, String) -> String = { s1, s2 -> "$s1.$s2" }
): String =
    tail?.let {
        val tailDisplay = it.joinToString(joiner)
        val headDisplay = head.value
        when (head) {
            is Index -> "$tailDisplay$headDisplay"
            else -> joiner(tailDisplay, headDisplay)
        }
    } ?: head.value

fun ValidationPath.lastSubPath(): String =
    when(head) {
        is Index -> (tail?.lastSubPath() ?: "") + head.value
        else -> head.value
    }

operator fun ValidationPath.iterator(): Iterator<ValidationPath> =
    object : Iterator<ValidationPath> {
        override fun hasNext(): Boolean = this@iterator.tail != null

        override fun next(): ValidationPath = this@iterator.tail!!
    }
