package io.dwsoft.checkt.core.validation.dsl

import io.dwsoft.checkt.core.check.Check
import io.dwsoft.checkt.core.validation.Displayed
import io.dwsoft.checkt.core.validation.NamingScope
import io.dwsoft.checkt.core.validation.ValidationError
import io.dwsoft.checkt.core.validation.ValidationResult
import io.dwsoft.checkt.core.validation.ValidationResult.Failure
import io.dwsoft.checkt.core.validation.toDisplayed
import io.dwsoft.checkt.core.validation.toNamingScope
import io.dwsoft.checkt.core.validation.toResult
import kotlin.reflect.KProperty0

// TODO:
//  * deal with non-null check without contracts
//  * create DSL methods
//  * support for suspension (for translation [and validation/checks ???])
//  * interface Validator for DTOs (idea for later ???)

sealed class ValidationScope(val name: Displayed) {
    private val _errors: MutableList<ValidationError<*, *, *>> = mutableListOf()
    val errors: List<ValidationError<*, *, *>>
        get() = _errors
    val result: ValidationResult
        get() = errors.toResult()

    fun mergeWith(other: ValidationScope) {
        val otherResult = other.result
        if (otherResult is Failure) {
            _errors.addAll(otherResult.errors)
        }
    }

    fun <V, K : Check.Key, P : Check.Params> checkValue(
        value: V,
        rule: ValidationRule<V, K, P>,
    ): ValidationError<V, K, P>? {
        val check = rule.check
        val errorDetailsBuilder = rule.errorDetailsBuilder
        return check(value)
            .takeIf { passes -> !passes }
            ?.let {
                val namingScope = this.toNamingScope()
                val errorDetailsBuilderContext = ErrorDetailsBuilderContext(value, namingScope, check.context)
                val errorDetails = errorDetailsBuilder(errorDetailsBuilderContext)
                ValidationError(value, namingScope, check.context, errorDetails)
            }
            ?.also { _errors.add(it) }
    }
}

private fun ValidationScope.toNamingScope(): NamingScope =
    name.toNamingScope(
        nestedIn = (this as? Nested)?.nestedIn?.toNamingScope()
    )

internal class Root(name: Displayed) : ValidationScope(name)

internal class Nested(
    name: Displayed,
    val nestedIn: ValidationScope,
) : ValidationScope(name)

fun <T> validate(
    value: T,
    namedAs: Displayed,
    validation: context(ValidationScope) T.() -> Unit,
): ValidationResult =
    validateInNewScope(value, namedAs, null, validation)

fun <T> validate(
    property: KProperty0<T>,
    validation: context(ValidationScope) T.() -> Unit,
): ValidationResult =
    validateInNewScope(
        value = property.get(),
        namedAs = property.toDisplayed(),
        parentScope = null,
        validation = validation
    )

context(ValidationScope)
fun <T> validate(
    value: T,
    namedAs: Displayed,
    validation: context(ValidationScope) T.() -> Unit,
): ValidationResult =
    validateInNewScope(value, namedAs, this@ValidationScope, validation)

context(ValidationScope)
fun <T> validate(
    property: KProperty0<T>,
    validation: context(ValidationScope) T.() -> Unit,
): ValidationResult =
    validateInNewScope(
        value = property.get(),
        namedAs = property.toDisplayed(),
        parentScope = this@ValidationScope,
        validation = validation
    )

private inline fun <T> validateInNewScope(
    value: T,
    namedAs: Displayed,
    parentScope: ValidationScope?,
    validation: context(ValidationScope) T.() -> Unit,
): ValidationResult {
    val validationScope =
        namedAs.toValidationScope(parentScope)
            .apply { validation(value) }
    return when (parentScope) {
        null -> validationScope.result
        else -> {
            parentScope.mergeWith(validationScope)
            parentScope.errors.toResult()
        }
    }
}

private fun Displayed.toValidationScope(nestedIn: ValidationScope? = null): ValidationScope =
    when (nestedIn) {
        null -> Root(this)
        else -> Nested(this, nestedIn)
    }
