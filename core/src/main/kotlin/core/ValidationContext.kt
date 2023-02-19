package io.dwsoft.checkt.core

sealed interface ValidationContext<C : Check<*>> {
    val key: Check.Key<C>
    val path: ValidationPath

    companion object {
        fun <C> create(check: C, validationPath: ValidationPath): ValidationContext<C>
                where C : Check<*> =
            Parameterless(check.key, validationPath)

        fun <C, P> create(check: C, validationPath: ValidationPath): ValidationContext<C>
                where C : ParameterizedCheck<*, P>, P : ParamsOf<C, P> =
            Parameterized(check.key, check.params, validationPath)
    }
}

private data class Parameterless<C : Check<*>>(
    override val key: Check.Key<C>,
    override val path: ValidationPath
) : ValidationContext<C>

private data class Parameterized<C : ParameterizedCheck<*, P>, P : ParamsOf<C, P>>(
    override val key: Check.Key<C>,
    val params: P,
    override val path: ValidationPath
) : ValidationContext<C>

@Suppress("UNCHECKED_CAST")
val <C, P> ValidationContext<C>.params: P
        where C : ParameterizedCheck<*, P>, P : ParamsOf<C, P>
    get() = (this as Parameterized<C, P>).params
