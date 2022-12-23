package io.dwsoft.checkt.core

sealed interface ValidationResult {
    val result: Result<ValidationStatus>
}

suspend fun validateCatching(block: suspend () -> ValidationStatus): ValidationResult =
    ValidationResultInternal(runCatching { block() })

fun ValidationResult.getOrThrow(): ValidationStatus = result.getOrThrow()

suspend fun Collection<ValidationResult>.fold(): ValidationResult =
    validateCatching {
        map { it.getOrThrow() }.fold()
    }

/**
 * Runs given [block] only when the [result][ValidationResult] represents valid
 * validation, i.e. completed without throwing any exceptions and is [successful
 * validation][ValidationStatus.Valid] (containing no [violations][Violation]).
 */
suspend fun ValidationResult.runWhenValid(
    block: suspend () -> ValidationResult,
): ValidationResult =
    validateCatching {
        when (val status = getOrThrow()) {
            is ValidationStatus.Valid -> block().getOrThrow()
            is ValidationStatus.Invalid -> status
        }
    }

/**
 * Runs given [block] when the [result][ValidationResult] represents failed
 * completion caused by exception of type [T]. Failure cause is passed to the
 * callback lambda as a parameter.
 */
inline fun <reified T : Throwable> ValidationResult.runWhenFailureOfType(
    block: (exception: T) -> ValidationResult,
): ValidationResult =
    result.fold(
        onSuccess = { this },
        onFailure = { exception ->
            when (exception) {
                is T -> block(exception)
                else -> this
            }
        }
    )

@JvmInline
private value class ValidationResultInternal(
    override val result: Result<ValidationStatus>
) : ValidationResult
