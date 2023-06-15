package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.ValidationRule.Descriptor

sealed interface ValidationContext<D : Descriptor<*, *, *>> {
    val descriptor: D
    val path: ValidationPath

    companion object {
        fun <D> create(descriptor: D, validationPath: ValidationPath): ValidationContext<D>
                where D : Descriptor<*, *, *> =
            Parameterless(descriptor, validationPath)

        fun <D, C, P> create(descriptor: D, params: P, validationPath: ValidationPath): ValidationContext<D>
                where D : Descriptor<*, C, *>, C : ParameterizedCheck<*, P>, P : ParamsOf<C, P> =
            Parameterized(descriptor, params, validationPath)
    }
}

private data class Parameterless<D : Descriptor<*, *, *>>(
    override val descriptor: D,
    override val path: ValidationPath
) : ValidationContext<D>

private data class Parameterized<D : Descriptor<*, C, *>, C : ParameterizedCheck<*, P>, P : ParamsOf<C, P>>(
    override val descriptor: D,
    val params: P,
    override val path: ValidationPath
) : ValidationContext<D>

@Suppress("UNCHECKED_CAST")
val <C, P> ValidationContext<out Descriptor<*, C, *>>.params: P
        where C : ParameterizedCheck<*, P>, P : ParamsOf<C, P>
    get() = (this as Parameterized<*, C, P>).params
