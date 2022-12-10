package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.ValidationResult.Failure
import io.dwsoft.checkt.core.ValidationResult.Success

sealed class ValidationResult {
    internal abstract infix operator fun plus(other: ValidationResult): ValidationResult

    object Success : ValidationResult() {
        override fun plus(other: ValidationResult) = other
    }

    data class Failure(val errors: List<ValidationError<*, *, *>>) : ValidationResult() {
        constructor(vararg errors: ValidationError<*, *, *>) : this(errors.asList())

        override fun plus(other: ValidationResult): ValidationResult =
            when (other) {
                Success -> this
                is Failure -> (this.errors + other.errors).toValidationResult()
            }
    }
}

fun Failure.errorMessages(): List<String> =
    errors.map { it.errorDetails }

fun List<ValidationError<*, *, *>>.toValidationResult(): ValidationResult =
    if (this.isEmpty()) Success else Failure(this)

fun <C : Check<V, P, C>, V, P : Check.Params<C>>
        ValidationError<C, V, P>?.toValidationResult(): ValidationResult =
    when (this) {
        null -> emptyList()
        else -> listOf(this)
    }.toValidationResult()

data class ValidationFailure(val errors: List<ValidationError<*, *, *>>) : RuntimeException()

fun ValidationResult.throwIfFailure(): Unit =
    when (this) {
        is Failure -> throw ValidationFailure(errors)
        Success -> Unit
    }

fun ValidationFailure.errorMessages(): List<String> =
    errors.map { it.errorDetails }

typealias ValidationStatus = Result<ValidationResult>

fun Collection<ValidationStatus>.fold(): ValidationStatus =
    when {
        this.isNotEmpty() -> {
            map {
                it.fold(
                    onFailure = { _ -> return it },
                    onSuccess = { validationResult -> return@map validationResult }
                )
            }.reduce(ValidationResult::plus)
        }
        else -> Success
    }.asValidationStatus()

fun ValidationResult.asValidationStatus(): ValidationStatus = Result.success(this)
