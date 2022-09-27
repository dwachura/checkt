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

        data class Name(override val value: String) : Segment

        data class Index(val rawValue: String) : Segment {
            override val value: String = "[$rawValue]"
        }
    }

    companion object
}

val NamingPath.Companion.unnamed: NamingPath
    get() = NamingPathImpl.empty

fun NamingPath.Companion.named(name: Segment.Name): NamingPath = NamingPath(name)

operator fun NamingPath.plus(segment: Segment): NamingPath =
    when (segment) {
        Segment.Empty -> this
        is Segment.Name -> NamingPath(segment, this)
        is Segment.Index -> {
            require(this != NamingPathImpl.empty) { "Empty path cannot be indexed" }
            NamingPath(segment, this)
        }
    }

fun NamingPath.fold(joiningSegments: (String, String) -> String): String =
    tail?.let {
        val tailDisplay = it.fold(joiningSegments)
        val headDisplay = head.value
        when {
            tailDisplay.isEmpty() -> headDisplay
            head is Segment.Empty -> tailDisplay
            head is Segment.Index -> "$tailDisplay$headDisplay"
            else -> joiningSegments(tailDisplay, headDisplay)
        }
    } ?: head.value

fun NamingPath.lastToken(): String =
    when (head) {
        is Segment.Empty, is Segment.Name -> head.value
        else -> {
            tail?.let {
                lastToken() + head.value
            } ?: head.value
        }
    }

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
