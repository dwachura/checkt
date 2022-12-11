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

fun List<Violation<*, *, *>>.toValidationResult(): ValidationResult =
    if (this.isEmpty()) Success else Failure(this)

fun <C : Check<V, P, C>, V, P : Check.Params<C>>
        Violation<C, V, P>?.toValidationResult(): ValidationResult =
    when (this) {
        null -> emptyList()
        else -> listOf(this)
    }.toValidationResult()

fun Collection<ValidationResult>.fold(): ValidationResult =
    takeIf { it.isNotEmpty() }
        ?.reduce(ValidationResult::plus)
        ?: Success

fun Failure.errorMessages(): List<String> =
    violations.map { it.errorMessage }

data class ValidationFailure(val violations: List<Violation<*, *, *>>) : RuntimeException()

fun ValidationResult.throwIfFailure(): Unit =
    when (this) {
        is Failure -> throw ValidationFailure(violations)
        Success -> Unit
    }

fun ValidationFailure.errorMessages(): List<String> =
    violations.map { it.errorMessage }

typealias ValidationStatus = Result<ValidationResult>

fun ValidationResult.asValidationStatus(): ValidationStatus = Result.success(this)

fun Collection<ValidationStatus>.fold(): ValidationStatus =
    map {
        it.fold(
            onFailure = { _ -> return it },
            onSuccess = { validationResult -> return@map validationResult }
        )
    }.fold().asValidationStatus()

/**
 * Runs given [block] only when the [status][ValidationStatus] represents valid
 * validation result, i.e. completed without throwing any exceptions and is
 * [successful validation][ValidationResult.Success] (containing no [violations][Violation]).
 */
suspend fun ValidationStatus.runWhenValid(
    block: suspend () -> ValidationStatus,
): ValidationStatus =
    fold(
        onSuccess = {
            when (it) {
                is Success -> block()
                is Failure -> it.asValidationStatus()
            }
        },
        onFailure = { return this }
    )

/**
 * Runs given [block] when the [status][ValidationStatus] represents failed
 * completion caused by exception of type [T]. Failure cause is passed to the
 * callback lambda as a parameter.
 */
inline fun <reified T : Throwable> ValidationStatus.runWhenFailureOfType(
    block: (exception: T) -> ValidationStatus,
): ValidationStatus =
    fold(
        onSuccess = { this },
        onFailure = { exception ->
            when (exception) {
                is T -> block(exception)
                else -> this
            }
        }
    )
