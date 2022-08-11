package io.dwsoft.checkt.core

data class NamingPath(val segments: List<Displayed>) {
    fun fold(
        transformingSegments: (Displayed) -> String,
        joiningSegments: (String, String) -> String
    ): String =
        when (this) {
            Empty -> ""
            else -> segments.map(transformingSegments).reduce(joiningSegments)
        }

    override fun toString(): String =
        fold(
            transformingSegments = { it.value },
            joiningSegments = { s, s2 -> "$s.$s2" }
        )

    companion object {
        val Empty = NamingPath(emptyList())
    }
}

fun Displayed?.toNamingPath() =
    when (this) {
        null, Displayed.Empty -> NamingPath.Empty
        else -> NamingPath(listOf(this))
    }

operator fun NamingPath.plus(segment: Displayed): NamingPath =
    NamingPath(segments + segment)
