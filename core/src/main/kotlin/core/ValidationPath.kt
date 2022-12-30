package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.ValidationPath.Element
import io.dwsoft.checkt.core.ValidationPath.Element.Index
import io.dwsoft.checkt.core.ValidationPath.Element.Key
import io.dwsoft.checkt.core.ValidationPath.Element.Segment
import io.dwsoft.checkt.core.ValidationPath.Element.NumericIndex

sealed interface ValidationPath {
    val head: Element
    val tail: ValidationPath?

    sealed interface Element {
        val value: String

        data class Segment(val rawValue: NotBlankString) : Element {
            override val value: String
                get() = rawValue.value
        }

        sealed interface Index : Element

        data class NumericIndex(val rawValue: Int) : Index {
            override val value: String = "[$rawValue]"
        }

        data class Key(val rawValue: NotBlankString) : Index {
            override val value: String = "[$rawValue]"
        }
    }

    companion object {
        private val rootPath: ValidationPath =
            ValidationPathInternal(Root, null)

        operator fun invoke(name: NotBlankString? = null): ValidationPath =
            name?.let { rootPath + Segment(name) } ?: rootPath

        operator fun invoke(head: Element) =
            rootPath + head
    }
}

private object Root : Element {
    override val value: String
        get() = Checkt.Settings.ValidationPath.rootDisplayedAs
}

private data class ValidationPathInternal(
    override val head: Element,
    override val tail: ValidationPath?
) : ValidationPath

operator fun ValidationPath.plus(element: Element): ValidationPath =
    ValidationPathInternal(element, this)

operator fun ValidationPath.plus(name: NotBlankString) = this + Segment(name)

fun NotBlankString.asSegment(): Segment = Segment(this)

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

object ValidationPathSettings {
    var rootDisplayedAs = "$"
}

val Checkt.Settings.ValidationPath
    get() = ValidationPathSettings
