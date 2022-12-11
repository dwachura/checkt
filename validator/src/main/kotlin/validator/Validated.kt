package io.dwsoft.checkt.validator

import io.dwsoft.checkt.core.ValidationSpecification
import io.dwsoft.checkt.core.ValidationStatus

interface Validated<V : Validated<V>> {
    val validator: Validator<V>
}

fun <T : Validated<*>> T.asSelfValidated(): SelfValidated? = SelfValidated.from(this)

class SelfValidated private constructor(private val validated: Validated<*>) {
    @Suppress("UNCHECKED_CAST")
    suspend fun validate(): ValidationStatus =
        (validated.validator as Validator<Any>).validate(validated)

    companion object {
        fun from(validated: Validated<*>): SelfValidated? =
            with(validated.validator) {
                if (supportsValue(validated)) SelfValidated(validated) else null
            }
    }
}

fun <T : Validated<T>> delegate(validator: Validator<T>): Validated<T> =
    object : Validated<T> {
        override val validator: Validator<T>
            get() = validator
    }

inline fun <reified T : Validated<T>> delegate(
    noinline spec: ValidationSpecification<T>
) = delegate(spec.asValidator())

inline fun <reified T : Validated<T>, S> delegate(
    noinline validating: T.() -> S,
    crossinline by: ValidationSpecification<S>,
): Validated<T> =
    delegate(Validator { by(validating(), it) })

inline fun <reified T : Validated<T>, S : Validated<S>> delegate(
    noinline validating: T.() -> S,
    by: Validator<S>,
) = delegate(validating, by.validationSpecification)
