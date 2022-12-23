package io.dwsoft.checkt.core

import kotlin.reflect.KProperty0

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

class Named<T>(val value: T, val name: NotBlankString)

fun <T> T.namedAs(name: NotBlankString): Named<T> = Named(this, name)

fun <T> KProperty0<T>.toNamed(): Named<T> = Named(get(), !name)
