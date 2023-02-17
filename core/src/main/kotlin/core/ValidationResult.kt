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
 *
 * [Unrecoverable exceptions][ValidationResultSettings.unrecoverableErrorTypes],
 * i.e. those that should not be handled by the user are rethrown.
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
 * Runs given [fallback operations][fallbacks] when the
 * [result][ValidationResult] represents exceptional completion.
 *
 * [Fallbacks][RecoveryFrom] passed should conform to the standard try-catch
 * rules regarding exception subtyping, i.e. fallback of more specific error
 * types should be placed before fallbacks used to recover from the general
 * error types. Otherwise, they won't be used to process exceptions cause those
 * are handled by the first fallback that supports given error type.
 */
suspend fun ValidationResult.catch(
    vararg fallbacks: RecoveryFrom<*>
): ValidationResult =
    validateCatching {
        result.fold(
            onSuccess = { this.getOrThrow() },
            onFailure = { exception ->
                fallbacks.find { it.errorType.isInstance(exception) }
                    ?.let {
                        @Suppress("UNCHECKED_CAST")
                        (it as RecoveryFrom<Throwable>).func(exception)
                    } ?: this.getOrThrow()
            }
        )
    }

class RecoveryFrom<E : Throwable>(
    val errorType: KClass<E>,
    val func: suspend (E) -> ValidationStatus
) {
    companion object {
        inline operator fun <reified T : Throwable> invoke(
            noinline func: suspend (exception: T) -> ValidationStatus,
        ): RecoveryFrom<T> = RecoveryFrom(T::class, func)
    }
}

@JvmInline
private value class ValidationResultInternal(
    override val result: Result<ValidationStatus>
) : ValidationResult

object ValidationResultSettings {
    /**
     * User-defined error types that should not be handled by [ValidationResult]
     * mechanism.
     */
    var customUnrecoverableErrorTypes: List<KClass<out Throwable>> = emptyList()

    /**
     * Default error types that should not be handled by [ValidationResult]
     * mechanism.
     */
    val baseUnrecoverableErrorTypes: List<KClass<out Throwable>> =
        listOf(
            Error::class,
            CancellationException::class,
            ValidationScope.NamingUniquenessException::class,
        )

    fun isUnrecoverable(throwable: Throwable): Boolean =
        throwable::class in unrecoverableErrorTypes

    /**
     * List of unrecoverable error types.
     *
     * Additional ones may be specified using [customUnrecoverableErrorTypes]
     * setting.
     */
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
