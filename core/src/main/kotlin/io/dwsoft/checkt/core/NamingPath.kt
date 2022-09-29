package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.NamingPath.Segment

sealed interface NamingPath {
    val head: Segment
    val tail: NamingPath?

    sealed interface Segment {
        val value: String

        object Empty : Segment {
            override val value: String = ""
        }

        data class Name(override val value: String) : Segment {
            init {
                require(value.isNotBlank()) { "Name of path segment cannot be blank" }
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

val NamingPath.Companion.unnamed: NamingPath
    get() = NamingPathImpl.empty

fun NamingPath.Companion.named(name: Segment.Name): NamingPath = NamingPath(name)

fun NamingPath.Companion.named(name: String): NamingPath = named(Segment.Name(name))

operator fun NamingPath.plus(segment: Segment): NamingPath =
    when (segment) {
        Segment.Empty -> this
        is Segment.Name -> NamingPath(segment, this)
        is Segment.Index -> {
            require(this != NamingPathImpl.empty) { "Empty path cannot be indexed" }
            NamingPath(segment, this)
        }
    }

fun NamingPath.joinToString(joiner: (String, String) -> String = { s1, s2 -> "$s1.$s2" }): String =
    tail?.let {
        val tailDisplay = it.joinToString(joiner)
        val headDisplay = head.value
        when {
            tailDisplay.isEmpty() -> headDisplay
            head is Segment.Empty -> tailDisplay
            head is Segment.Index -> "$tailDisplay$headDisplay"
            else -> joiner(tailDisplay, headDisplay)
        }
    } ?: head.value

fun NamingPath.lastToken(): String =
    when (head) {
        is Segment.Empty, is Segment.Name -> head.value
        else -> {
            tail?.let {
                it.lastToken() + head.value
            } ?: head.value
        }
    }

fun NamingPath.toList(): List<Segment> =
    (tail?.toList() ?: emptyList()) + head

private fun NamingPath(head: Segment, tail: NamingPath? = null): NamingPath =
    NamingPathImpl(head, tail)

private data class NamingPathImpl(
    override val head: Segment,
    override val tail: NamingPath?,
) : NamingPath {
    init {
        if (tail == null) require(head !is Segment.Index) { "Naming path cannot start with index" }
    }

    companion object {
        val empty = NamingPath(Segment.Empty)
    }
}
