package io.dwsoft.checkt.core

data class ValidationError<C : Check<V, P, C>, V, P : Check.Params<C>>(
    val validatedValue: V,
    val validationContext: ValidationContext<C, P>,
    val validationPath: ValidationPath,
    val errorDetails: String,
)
