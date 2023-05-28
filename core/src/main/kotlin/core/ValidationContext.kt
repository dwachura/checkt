package io.dwsoft.checkt.core

sealed interface ValidationContext<D : ValidationRule.Descriptor<*, C>, C : Check<*>> {
    val descriptor: D
    val path: ValidationPath

    companion object {
        fun <D, C> create(descriptor: D, validationPath: ValidationPath): ValidationContext<D, C>
                where D : ValidationRule.Descriptor<*, C>, C : Check<*> =
            Parameterless(descriptor, validationPath)

        fun <D : ValidationRule.Descriptor<*, C>, C: ParameterizedCheck<*, P>, P : ParamsOf<C, P>> create(
            descriptor: D,
            params: P,
            validationPath: ValidationPath
        ): ValidationContext<D, C> =
            Parameterized(descriptor, params, validationPath)
    }
}

private data class Parameterless<D : ValidationRule.Descriptor<*, C>, C : Check<*>>(
    override val descriptor: D,
    override val path: ValidationPath
) : ValidationContext<D, C>

private data class Parameterized<D : ValidationRule.Descriptor<*, C>, C : ParameterizedCheck<*, P>, P : ParamsOf<C, P>>(
    override val descriptor: D,
    val params: P,
    override val path: ValidationPath
) : ValidationContext<D, C>

@Suppress("UNCHECKED_CAST")
val <C, P> ValidationContext<out ValidationRule.Descriptor<*, C>, C>.params: P
        where C : ParameterizedCheck<*, P>, P : ParamsOf<C, P>
    get() = (this as Parameterized<*, C, P>).params
