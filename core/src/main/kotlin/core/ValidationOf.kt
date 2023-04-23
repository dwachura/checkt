package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.ValidationOf.Internals
import kotlin.reflect.KClass
import kotlin.reflect.KProperty0

/**
 * Validation specification represents a reusable validation logic definition,
 * i.e. a function validating value under named scope.
 */
typealias ValidationSpecification<T> = suspend T.(NotBlankString?) -> ValidationResult

/**
 * Entry point of defining [validation logic][ValidationSpecification] for values
 * of type [T].
 */
fun <T> validation(validationBlock: ValidationBlock<T>): ValidationSpecification<T> =
    spec@{ namedAs ->
        validateCatching {
            ValidationScope(ValidationPath(namedAs)).apply {
                toValidationOf(this@spec).validationBlock()
            }.status
        }
    }

/**
 * Shortcut for immediate invocation of [validation specification][validation].
 */
suspend fun <T> T.validate(
    namedAs: NotBlankString? = null,
    validationBlock: ValidationBlock<T>,
): ValidationResult =
    validation(validationBlock)(this, namedAs)

/**
 * Validation logic builder/DSL.
 *
 * It creates validation context composed of validated value, i.e. [subject] together
 * with a [named scope][ValidationScope] within which defined rules are executed.
 *
 * [ValidationOf] is also [validation rules "namespace"][ValidationRules], giving
 * access to the variety of [validation rules][ValidationRule] DSL functions.
 *
 * Instances of this class are also of [ValidationOf.Internals] type that
 * provides access to the internal implementation properties (like [validation scope]
 * [Internals.scope]). This trick lets to hide internal stuff when DSL is used to
 * defining validation logic, but let them be available when needed (e.g. to define
 * custom extension functions).
 */
sealed class ValidationOf<V>(val subject: V) : ValidationRules<V> {
    /**
     * Checks the [currently validated value][subject] against given
     * [validation rule][this].
     */
    suspend operator fun <C> ValidationRule<V, C>.unaryPlus(): ValidationStatus
            where C : Check<*> =
        with(internalsGate) {
            scope.verifyValue(subject, this@unaryPlus)
        }

    /**
     * Validates value (passed as a receiver) against given [validation logic]
     * [validationBlock], executed under the current context's
     * [naming][ValidationScope.validationPath].
     */
    suspend operator fun <T> T.invoke(
        validationBlock: ValidationBlock<T>
    ): ValidationStatus =
        with(internalsGate) {
            scope.validate {
                toValidationOf(this@invoke).validationBlock()
            }
        }

    /**
     * Validate given [value][namedValue] against [validation logic][validationBlock]
     * into a new, [named][Named.name] [context][ValidationOf] relative to the
     * current one.
     *
     * New naming must be unique under the current context, [unrecoverable error]
     * [ValidationResultSettings.unrecoverableErrorTypes] is thrown otherwise (see
     * [ValidationScope.validate]).
     */
    suspend fun <T> require(
        namedValue: Named<T>,
        validationBlock: ValidationBlock<T>
    ): ValidationStatus =
        with(internalsGate) {
            scope.validate(namedValue.name.asSegment()) {
                toValidationOf(namedValue.value).validationBlock()
            }
        }

    /**
     * "Overloaded" version of [require] function that takes property as
     * a parameter. A new scope's name and value to validate is taken from the
     * property specified ([KProperty0.name] and [KProperty0.get] respectively).
     */
    suspend fun <T> require(
        property: KProperty0<T>,
        validationBlock: ValidationBlock<T>
    ): ValidationStatus =
        require(property.toNamed(), validationBlock)

    /**
     * DSL version of [runWhenValid] making [current context][ValidationOf]
     * available into passed [validation block][validationBlock].
     */
    suspend infix fun ValidationStatus.whenValid(
        validationBlock: ValidationBlock<V>
    ): ValidationStatus =
        takeIf { it is ValidationStatus.Invalid }
            ?: subject.invoke(validationBlock)

    /**
     * Runs given [validation logic][validationBlock] in a safe manner, catching
     * exceptions thrown, that can be [recovered][recover] later.
     */
    suspend fun catching(validationBlock: ValidationBlock<V>): ValidationResult =
        validateCatching {
            subject.invoke(validationBlock)
        }

    /**
     * DSL version of [catch] making [current context][ValidationOf]
     * available into passed [fallback operations][ValidationFallback].
     *
     * Fallbacks passed should conform to the same rules that [catch] function
     * defines.
     */
    suspend fun ValidationResult.recover(
        vararg fallbacks: ValidationFallback<V, *>
    ): ValidationStatus {
        val convertedFallbacks = fallbacks.map { fallback ->
            RecoveryFrom(fallback.errorType) {
                subject {
                    @Suppress("UNCHECKED_CAST")
                    (fallback.func as ValidationBlock1<V, Throwable>)(it)
                }
            }
        }.toTypedArray()
        return catch(*convertedFallbacks).getOrThrow()
    }

    /**
     * Factory of [validation fallbacks][ValidationFallback] for errors of
     * type [T].
     */
    inline fun <reified T : Throwable> from(
        noinline func: ValidationBlock1<V, T>
    ): ValidationFallback<V, T> =
        ValidationFallback(T::class, func)

    sealed class Internals<V>(
        subject: V,
        val scope: ValidationScope
    ) : ValidationOf<V>(subject)
}

/**
 * Validates each element of given iterable (being a [subject][ValidationOf.subject]
 * of a current context) against provided [validation rules][block].
 * Each element is validated into new scope indexed according to its position in the
 * iterable. Index of currently validated element is passed to the validation block
 * as a lambda parameter.
 */
suspend fun <T : Iterable<EL>, EL> ValidationOf<T>.eachElement(
    block: ValidationBlock1<EL, Int>,
): ValidationStatus =
    with(internalsGate) {
        subject.mapIndexed { idx, value ->
            scope.validate(idx.asIndex()) {
                toValidationOf(value).block(idx)
            }
        }.fold()
    }

/**
 * Validates each entry of given map (being a [subject][ValidationOf.subject] of
 * a current context) against provided validation rules.
 *
 * Rules can be defined for separately entry's key and value (the former is optional).
 * In both cases "opposite" entry side (value in case of a key, and vice-versa) is
 * passed to the validation lambda as a parameter.
 *
 * Each entry is validated into new scope indexed with entry key transformed by the
 * given [transforming function][displayingKeysAs]. Entry's key and value also have
 * their own scopes created named "key" and "value" respectively.
 */
suspend fun <T : Map<K, V>, K, V> ValidationOf<T>.eachEntry(
    displayingKeysAs: (key: K) -> NotBlankString = { key -> !(key.toString()) },
    keyValidation: ValidationBlock1<K, V>? = null,
    valueValidation: ValidationBlock1<V, K>,
): ValidationStatus =
    with(internalsGate) {
        subject.map { entry ->
            val key = entry.key
            val value = entry.value
            scope.validate(displayingKeysAs(key).asKey()) {
                val valueValidationStatus = validate((!"value").asSegment()) {
                    toValidationOf(value).valueValidation(key)
                }
                val keyValidationStatus = keyValidation?.let {
                    validate((!"key").asSegment()) {
                        toValidationOf(key).keyValidation(value)
                    }
                } ?: ValidationStatus.Valid
                valueValidationStatus + keyValidationStatus
            }
        }.fold()
    }

// TODO: tests
/**
 * Validates given [value][namedValue] unless it's null.
 *
 * Returns [ValidationStatus.Valid] otherwise.
 */
suspend fun <T> ValidationOf<*>.requireUnlessNull(
    namedValue: Named<T?>,
    validationBlock: ValidationBlock<T>
): ValidationStatus =
    namedValue.value?.let {
        require(it.namedAs(namedValue.name), validationBlock)
    } ?: ValidationStatus.Valid

// TODO: tests
/**
 * "Overloaded" version of [requireUnlessNull] function that takes property as
 * a parameter.
 */
suspend fun <T> ValidationOf<*>.requireUnlessNull(
    property: KProperty0<T?>,
    validationBlock: ValidationBlock<T>
): ValidationStatus =
    requireUnlessNull(property.toNamed(), validationBlock)

/**
 * Shorter alias for [validated subject][ValidationOf.subject], that can be
 * more convenient to use to retrieve its properties to validate.
 */
val <T> ValidationOf<T>.the: T
    get() = subject

typealias ValidationBlock<T> = suspend ValidationOf<T>.() -> Unit

typealias ValidationBlock1<T, T2> = suspend ValidationOf<T>.(T2) -> Unit

private val <T> ValidationOf<T>.internalsGate: Internals<T>
    get() = this as Internals<T>

private class ValidationOfInternal<V>(
    subject: V,
    scope: ValidationScope
) : Internals<V>(subject, scope)

private fun <T> ValidationScope.toValidationOf(value: T): ValidationOf<T> =
    ValidationOfInternal(value, this)

class ValidationFallback<V, E : Throwable>(
    val errorType: KClass<E>,
    val func: ValidationBlock1<V, E>
)
