package io.dwsoft.checkt.core.validation

import kotlin.reflect.KProperty

interface Displayed {
    val value: String

    private class Const(override val value: String) : Displayed

    companion object {
        fun of(value: String): Displayed = Const(value)
    }
}

fun String.toDisplayed(): Displayed = Displayed.of(this)

fun KProperty<*>.toDisplayed(): Displayed = name.toDisplayed()
