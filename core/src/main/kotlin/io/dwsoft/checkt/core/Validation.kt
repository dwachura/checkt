package io.dwsoft.checkt.core

import kotlin.reflect.KProperty0

/*
 * TODO:
 *  - correct docks after context removal and receivers refactor
 *  - tests and refactoring collection/map validation in regards of Result
 *  - tests of conditional rule processing
 */

/**
 * Validation specification represents a function validating value [T] under named scope.
 * Alias introduced for better readability and easier extension writing.
 */
typealias ValidationSpec<T> = suspend (T, NonBlankString?) -> Result<ValidationResult>

/**
 * Entry point of defining [validation logic][ValidationSpec] for values of type [T].
 */
fun <T> validationSpec(validation: ValidationBlock<T>): ValidationSpec<T> =
    { value, namedAs ->
        runCatching {
            val scope = namedAs?.let { ValidationScope(it) } ?: ValidationScope()
            Validation(value, scope).apply { validation().value.getOrThrow() }
            scope.result
        }
    }

/**
 * Validates given [value] in an optionally named scope against [defined rules][this].
 *
 * Alias of [ValidationSpec.invoke].
 */
suspend fun <T> ValidationSpec<T>.validate(value: T, namedAs: NonBlankString? = null) =
    invoke(value, namedAs)

/**
 * "Overloaded" version of [ValidationSpec.validate].
 */
suspend fun <T> T.validate(
    namedAs: NonBlankString? = null,
    with: ValidationSpec<T>,
): Result<ValidationResult> = with.validate(this, namedAs)

suspend fun <T> NamedValue<T>.validate(with: ValidationSpec<T>) = with(value, name)

/**
 * Validates given value against [spec][ValidationSpec] created out of passed [block]
 * [ValidationBlock].
 */
suspend fun <T> T.validate(
    namedAs: NonBlankString? = null,
    validation: ValidationBlock<T>,
): Result<ValidationResult> = validate(namedAs, validationSpec(validation))

// Todo: add dsl maker???
/** Todo
 * [Validation] runner for a given [value][subject].
 */
class Validation<V>(
    val subject: V,
    private val scope: ValidationScope
) {
    /**
     * Opens and returns a new, named [scope][ValidationScope] enclosed into contextual
     * scope, used to validate [value, taken from the receiver of this function][NamedValue.value].
     * The new scope's name is [taken from the receiver as well][NamedValue.name].
     *
     * [Validation block][validation] (containing validation logic) runs in a context
     * of the new scope and [validation DSL][Validation].
     */
    suspend infix fun <T> NamedValue<T>.require(validation: ValidationBlock<T>):
            ValidationBlockResult<ValidationResult> =
        runEnclosed(value, ValidationPath.Segment.Name(name), validation)

    internal suspend fun <T> runEnclosed(
        value: T,
        namedAs: ValidationPath.Segment,
        validation: ValidationBlock<T>,
    ): ValidationBlockResult<ValidationResult> =
        runCatching {
            scope.enclose(namedAs).also { enclosedScope ->
                validation(Validation(value, enclosedScope)).value.getOrThrow()
            }.result
        }.asValidationBlockResult()

    /**
     * "Overloaded" version of [NamedValue.require] function that takes property as
     * a receiver. A new scope's name and value to validate is taken from the property
     * specified ([KProperty0.name] and [KProperty0.get] respectively).
     */
    suspend infix fun <T> KProperty0<T>.require(
        validation: ValidationBlock<T>
    ): ValidationBlockResult<ValidationResult> =
        get().namedAs(!name).require(validation)

    /**
     * Function used to apply given [validation rule][toBeValidAgainst] to the validated [value][this].
     *
     * Registers and returns [error][ValidationError] if the value doesn't conform to the given rule
     * or returns null otherwise.
     */
    infix fun <C : Check<T, P, C>, T : Any?, P : Check.Params<C>> T.require(
        toBeValidAgainst: ValidationRule<C, T, P>,
    ): ValidationBlockResult<ValidationError<C, T, P>?> =
        checkValue(this, toBeValidAgainst)

    /**
     * Shortcut to apply [V.requireTo][Any.require] on a [currently validated value]
     * [subject].
     */
    operator fun <C : Check<V, P, C>, P : Check.Params<C>> ValidationRule<C, V, P>.unaryPlus() =
        subject.require(toBeValidAgainst = this)

    private fun <C : Check<T, P, C>, T : Any?, P : Check.Params<C>> checkValue(
        value: T,
        beValidAgainst: ValidationRule<C, T, P>,
    ): ValidationBlockResult<ValidationError<C, T, P>?> =
        runCatching {
            scope.checkValueAgainstRule(value, beValidAgainst)
        }.asValidationBlockResult()
}

/**
 * Returns a list of [indexed][ValidationPath.Segment.Index] [scopes][ValidationScope]
 * enclosed into contextual scope opened for each element contained into iterable that
 * is a receiver of this function.
 *
 * New scopes are indexed according to their position in the iterable.
 *
 * [Validation block][validation] (containing validation logic) runs in a context of
 * the new scope and [validation DSL][Validation]. Index of validated element
 * is passed to the block via parameter.
 */
suspend fun <T : Iterable<EL>, EL> Validation<T>.eachElement(
    validation: ValidationBlock1<EL, Int>,
): ValidationBlockResult<ValidationResult> =
    subject.mapIndexed { idx, value ->
        runEnclosed(value, ValidationPath.Segment.Index(idx)) {
            validation(idx)
        }
    }.fold()

/**
 * Returns a map of [indexed][ValidationPath.Segment.Index] [scopes][ValidationScope]
 * enclosed into contextual scope opened for each entry of the map that is passed
 * a receiver of this function.
 *
 * New scopes are indexed with entry key transformed by the given [transforming function]
 * [displayingKeysAs].
 *
 * Validation blocks (containing validation logic) [for entry key][keyValidation] and
 * [value][valueValidation] run in a context of the new scope and
 * [validation DSL][Validation]. Validated values are passed to the lambdas as
 * a receivers. Also, both blocks have access to a value from the "opposite" side (value
 * for key, key for value) accessible via lambda's parameter.
 */
suspend fun <T : Map<K, V>, K, V> Validation<T>.eachEntry(
    displayingKeysAs: (key: K) -> NonBlankString = ::defaultKeyTransformer,
    keyValidation: ValidationBlock1<K, V>? = null,
    valueValidation: ValidationBlock1<V, K>,
): ValidationBlockResult<ValidationResult> =
    subject.map { entry ->
        val key = entry.key
        val indexSegment = ValidationPath.Segment.Index(displayingKeysAs(key))
        runEnclosed(entry, indexSegment) newScope@{
            val value = entry.value
            val keyValidationResult = keyValidation?.let {
                this@newScope.runEnclosed(
                    key,
                    ValidationPath.Segment.Name(!"key")
                ) keyScope@{ this@keyScope.keyValidation(value) }
            }?.value?.getOrThrow()
            val valueValidationResult = runEnclosed(
                value,
                ValidationPath.Segment.Name(!"value")
            ) valueScope@{ this@valueScope.valueValidation(key) }
                .value.getOrThrow()
            (keyValidationResult?.let { it + valueValidationResult } ?: valueValidationResult)
                .asValidationBlockResult()
        }
    }.fold()

fun <T> defaultKeyTransformer(key: T): NonBlankString =
    NonBlankString(key?.toString() ?: "null")

class NamedValue<T>(val value: T, val name: NonBlankString)

fun <T> T.namedAs(name: NonBlankString): NamedValue<T> = NamedValue(this, name)

typealias ValidationBlock<V> = suspend Validation<V>.() -> ValidationBlockResult<*>

typealias ValidationBlock1<V, P1> = suspend Validation<V>.(P1) -> ValidationBlockResult<*>

sealed interface ValidationBlockResult<T> {
    val value: Result<T>
}

private fun Collection<ValidationBlockResult<ValidationResult>>.fold():
        ValidationBlockResult<ValidationResult> =
    map {
        it.value.fold(
            onFailure = { _ -> return it },
            onSuccess = { validationResult -> return@map validationResult }
        )
    }.reduce(
        ValidationResult::plus
    ).asValidationBlockResult()

@JvmInline
private value class ValidationBlockResultInternal<T>(
    override val value: Result<T>
) : ValidationBlockResult<T>

private fun <T> Throwable.asValidationBlockResult() = ValidationBlockResultInternal<T>(Result.failure(this))

private fun <T> Result<T>.asValidationBlockResult() = ValidationBlockResultInternal(this)

private fun ValidationResult.asValidationBlockResult() = ValidationBlockResultInternal(Result.success(this))
