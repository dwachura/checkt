package io.dwsoft.checkt.core

data class Violation<D : ValidationRule.Descriptor<V, C>, V, C : Check<*>>(
    val value: V,
    val context: ValidationContext<D, C>,
    val errorMessage: String,
)

fun <D : ValidationRule.Descriptor<V, C>, R, V, C : Check<V>> Violation<*, *, *>.ifFailedFor(
    ruleDescriptor: D,
    then: Violation<D, V, C>.() -> R
): R? =
    takeIf { it.context.descriptor == ruleDescriptor }
        ?.let {
            @Suppress("UNCHECKED_CAST")
            it as? Violation<D, V, C>
        }?.then()
