package io.dwsoft.checkt.core

import kotlin.reflect.KClass

data class Violation<D : ValidationRule.Descriptor<V, *, D>, V>(
    val value: V,
    val context: ValidationContext<D>,
    val errorMessage: String,
)

inline fun <D : ValidationRule.Descriptor<V, *, D>, V, R> Violation<*, *>.ifFailedFor(
    rule: D,
    then: Violation<D, V>.() -> R
): R? =
    ifFailedFor(rule::class, then)

inline fun <D : ValidationRule.Descriptor<V, *, D>, V, R> Violation<*, *>.ifFailedFor(
    ruleType: KClass<out D>,
    then: Violation<D, V>.() -> R
): R? =
    takeIf { it.context.descriptor::class == ruleType }
        ?.let {
            @Suppress("UNCHECKED_CAST")
            it as? Violation<D, V>
        }?.then()

inline fun <reified D : ValidationRule.Descriptor<V, *, D>, V, R> Violation<*, *>.ifFailedFor(
    then: Violation<D, V>.() -> R
): R? =
    ifFailedFor(D::class, then)
