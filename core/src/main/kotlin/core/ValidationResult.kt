package io.dwsoft.checkt.core

import kotlin.coroutines.cancellation.CancellationException
import kotlin.reflect.KClass

/**
 * Wrapper of [ValidationStatus] that represent a result of the *execution* of
 * a validation logic.
 *
 * The difference between [ValidationResult] and [ValidationStatus] is that the
 * former can be viewed as a "software-aware" version of the latter - besides
 * holding info about validation logic result/status, [ValidationResult] instances,
 * can represent [exceptional execution of the validation operations][Result].
 *
 * Instances of this class can be obtained via [dedicated factory function]
 * [validateCatching].
 */
sealed interface ValidationResult {
    val result: Result<ValidationStatus>
}

/**
 * Executes given [validation block][block], catching all eventual "standard"
 * exceptions thrown during it and returning [ValidationResult].
 */
suspend fun validateCatching(
    block: suspend () -> ValidationStatus
): ValidationResult =
    ValidationResultInternal(
        runCatching { block() }
    ).throwUnrecoverableError()

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

object ValidationResultSettings {
    /**
     * User-defined error types that should not be caught by [ValidationResult].
     */
    var customUnrecoverableErrorTypes: List<KClass<out Throwable>> = emptyList()

    /**
     * Default error types that should not be caught by [ValidationResult].
     */
    val baseUnrecoverableErrorTypes: List<KClass<out Throwable>> =
        listOf(Error::class, CancellationException::class)

    fun isUnrecoverable(throwable: Throwable): Boolean =
        throwable::class in unrecoverableErrorTypes

    private val unrecoverableErrorTypes
        get() = (baseUnrecoverableErrorTypes + customUnrecoverableErrorTypes)
}

val Checkt.Settings.ValidationResult
    get() = ValidationResultSettings

private fun ValidationResult.throwUnrecoverableError(): ValidationResult =
    apply {
        result.onFailure {
            if (ValidationResultSettings.isUnrecoverable(it)) throw it
        }
    }
