package io.dwsoft.checkt.core.validation

import io.dwsoft.checkt.core.validation.NamingScope.Nested
import io.dwsoft.checkt.core.validation.NamingScope.Root

sealed interface NamingScope {
    val name: Displayed

    data class Root(override val name: Displayed) : NamingScope

    data class Nested(
        override val name: Displayed,
        val nestedIn: NamingScope,
    ) : NamingScope
}

fun Displayed.toNamingScope(nestedIn: NamingScope? = null): NamingScope =
    when (nestedIn) {
        null -> Root(this)
        else -> Nested(this, nestedIn)
    }

fun NamingScope.validationPath(separator: String): String =
    when (this) {
        is Root -> name.value
        is Nested -> {
            val parentPath = nestedIn.validationPath(separator)
            "$parentPath$separator${name.value}"
        }
    }
