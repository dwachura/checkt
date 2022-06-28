package io.dwsoft.checkt.core.validation

import io.dwsoft.checkt.core.validation.ValidationResult.Failure
import io.dwsoft.checkt.core.validation.ValidationResult.Success

sealed class ValidationResult {
    object Success : ValidationResult()

    data class Failure(val errors: Collection<ValidationError<*, *, *>>) : ValidationResult()
}

fun Collection<ValidationError<*, *, *>>.toResult(): ValidationResult =
    if (this.isEmpty()) Success else Failure(this)
