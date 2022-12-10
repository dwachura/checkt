package io.dwsoft.checkt.validator

import io.dwsoft.checkt.core.ValidationSpecification
import io.dwsoft.checkt.core.ValidationStatus

interface Validated<V : Validated<V>> {
    val validator: Validator<V>
}

fun <T : Validated<T>> delegatingTo(validator: Validator<T>): Validated<T> =
    object : Validated<T> {
        override val validator: Validator<T>
            get() = validator
    }

// TODO: delegatingTo { value }.validatedBy(spec/validator) 
inline fun <reified T : Validated<T>, S> delegatingTo(
    noinline withSubject: T.() -> S,
    crossinline validatedBy: ValidationSpecification<S>,
): Validated<T> =
    delegatingTo(
        Validator { validatedBy(withSubject(), it) }
    )

class SelfValidated private constructor(private val validated: Validated<*>) {
    @Suppress("UNCHECKED_CAST")
    suspend fun validate(): ValidationStatus =
        (validated.validator as Validator<Any>).validate(validated)

    companion object {
        fun of(validated: Validated<*>): SelfValidated? =
            with(validated.validator) {
                if (supportsClass(validated::class)) SelfValidated(validated) else null
            }
    }
}

fun <T : Validated<T>> Validated<T>.asSelfValidated() = SelfValidated.of(this)
