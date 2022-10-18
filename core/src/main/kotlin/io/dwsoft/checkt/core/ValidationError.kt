package io.dwsoft.checkt.core

data class ValidationError<V, K : Check.Key, P : Check.Params>(
    val validatedValue: V,
    val violatedCheck: Check.Context<K, P>,
    val validationPath: ValidationPath,
    val errorDetails: String,
)
