package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.ValidationResult.Failure
import io.dwsoft.checkt.core.ValidationResult.Success

sealed class ValidationResult {
    internal abstract infix operator fun plus(other: ValidationResult): ValidationResult

    object Success : ValidationResult() {
        override fun plus(other: ValidationResult) = other
    }

    data class Failure(val violations: List<Violation<*, *, *>>) : ValidationResult() {
        constructor(vararg violations: Violation<*, *, *>) : this(violations.asList())

        override fun plus(other: ValidationResult): ValidationResult =
            when (other) {
                Success -> this
                is Failure -> (this.violations + other.violations).toValidationResult()
            }
    }
}

fun Failure.errorMessages(): List<String> =
    violations.map { it.errorMessage }

fun List<Violation<*, *, *>>.toValidationResult(): ValidationResult =
    if (this.isEmpty()) Success else Failure(this)

fun <C : Check<V, P, C>, V, P : Check.Params<C>>
        Violation<C, V, P>?.toValidationResult(): ValidationResult =
    when (this) {
        null -> emptyList()
        else -> listOf(this)
    }.toValidationResult()

data class ValidationFailure(val violations: List<Violation<*, *, *>>) : RuntimeException()

fun ValidationResult.throwIfFailure(): Unit =
    when (this) {
        is Failure -> throw ValidationFailure(violations)
        Success -> Unit
    }

fun ValidationFailure.errorMessages(): List<String> =
    violations.map { it.errorMessage }

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
