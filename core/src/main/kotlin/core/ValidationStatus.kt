package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.ValidationStatus.Invalid
import io.dwsoft.checkt.core.ValidationStatus.Valid

/**
 * Representation of validation logic result that may be either [Valid] or [Invalid].
 */
sealed class ValidationStatus {
    internal abstract infix operator fun plus(other: ValidationStatus): ValidationStatus

    object Valid : ValidationStatus() {
        override fun plus(other: ValidationStatus) = other
    }

    data class Invalid(val violations: List<Violation<*, *>>) : ValidationStatus() {
        constructor(
            violation: Violation<*, *>,
            vararg violations: Violation<*, *>
        ) : this(buildList {
            add(violation)
            addAll(violations)
        })

        override fun plus(other: ValidationStatus): Invalid =
            when (other) {
                Valid -> this
                is Invalid -> Invalid(this.violations + other.violations)
            }
    }
}

fun List<Violation<*, *>>.toValidationStatus(): ValidationStatus =
    if (this.isEmpty()) Valid else Invalid(this)

fun <C : Check<V>, V>
        Violation<C, V>?.toValidationStatus(): ValidationStatus =
    when (this) {
        null -> emptyList()
        else -> listOf(this)
    }.toValidationStatus()

fun Collection<ValidationStatus>.fold(): ValidationStatus =
    takeIf { it.isNotEmpty() }
        ?.reduce(ValidationStatus::plus)
        ?: Valid

fun Invalid.errorMessages(): List<String> =
    violations.map { it.errorMessage }

data class ValidationInvalid(val violations: List<Violation<*, *>>) : RuntimeException()

fun ValidationStatus.throwIfInvalid(): Unit =
    when (this) {
        is Invalid -> throw ValidationInvalid(violations)
        Valid -> Unit
    }

fun ValidationInvalid.errorMessages(): List<String> =
    violations.map { it.errorMessage }
