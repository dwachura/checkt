package io.dwsoft.checkt.core

import kotlin.reflect.KProperty

interface Displayed {
    val value: String

    object Empty : Displayed by Literal("")
}

private data class Literal(override val value: String) : Displayed

fun Displayed(value: String): Displayed = if (value.isEmpty()) Displayed.Empty else Literal(value)

fun String.toDisplayed(): Displayed = Displayed(this)

fun KProperty<*>.toDisplayed(): Displayed = name.toDisplayed()
