package io.dwsoft.checkt.core

data class Violation<C : Check<V, P, C>, V, P : Check.Params<C>>(
    val validatedValue: V,
    val validationContext: Context<C, P>,
    val errorMessage: String,
) {
    data class Context<C : Check<*, P, C>, P : Check.Params<C>>(
        val key: Check.Key<C>,
        val params: P,
        val path: ValidationPath,
    ) {
        companion object {
            operator fun <C : Check<*, P, C>, P : Check.Params<C>> invoke(
                check: C,
                validationPath: ValidationPath,
            ): Context<C, P> =
                Context(check.key, check.params, validationPath)
        }
    }
}
