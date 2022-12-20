package io.dwsoft.checkt.core

sealed interface ValidationResult {
    val result: Result<ValidationStatus>
}

suspend fun validateCatching(block: suspend () -> ValidationStatus): ValidationResult =
    ValidationResultInternal(runCatching { block() })

fun ValidationStatus.asValidationResult(): ValidationResult =
    ValidationResultInternal(Result.success(this))

fun ValidationResult.getOrThrow(): ValidationStatus = result.getOrThrow()

fun Collection<ValidationResult>.fold(): ValidationResult =
    map {
        it.result.fold(
            onFailure = { _ -> return it },
            onSuccess = { validationResult -> return@map validationResult }
        )
    }.fold().asValidationResult()

/**
 * Runs given [block] only when the [result][ValidationResult] represents valid
 * validation, i.e. completed without throwing any exceptions and is [successful
 * validation][ValidationStatus.Valid] (containing no [violations][Violation]).
 */
suspend fun ValidationResult.runWhenValid(
    block: suspend () -> ValidationResult,
): ValidationResult =
    result.fold(
        onSuccess = {
            when (it) {
                is ValidationStatus.Valid -> block()
                is ValidationStatus.Invalid -> it.asValidationResult()
            }
        },
        onFailure = { return this }
    )

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
