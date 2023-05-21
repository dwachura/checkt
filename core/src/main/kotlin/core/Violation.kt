package io.dwsoft.checkt.core

import kotlin.reflect.KClass

data class Violation<C : Check<*>, V>(
    val value: V,
    val context: ValidationContext<C>,
    val errorMessage: String,
)

@Suppress("UNCHECKED_CAST")
fun <T : Check<V>, V, R> Violation<*, *>.ifFailedFor(
    checkType: KClass<T>,
    then: Violation<T, V>.() -> R
): R? =
    takeIf { it.context.key.checkClass == checkType }
        ?.let { it as? Violation<T, V> }
        ?.then()

inline fun <reified T : Check<V>, V, R> Violation<*, *>.ifFailedFor(
    noinline then: Violation<T, V>.() -> R
): R? =
    ifFailedFor(T::class, then)
