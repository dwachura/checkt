package io.dwsoft.checkt.core

data class Violation<C : Check<V>, V>(
    val value: V,
    val context: ValidationContext<C>,
    val errorMessage: String,
)
