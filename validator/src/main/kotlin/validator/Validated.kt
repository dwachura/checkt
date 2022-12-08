package io.dwsoft.checkt.validator

import io.dwsoft.checkt.core.ValidationStatus

interface Validated<V : Any> {
    val validator: Validator<V>
}

//TODO: hide
class SelfValidated (private val validated: Validated<*>) {
    suspend fun validate(): ValidationStatus = (validated.validator as Validator<Any>).validate(validated)
}

fun <T : Validated<T>> delegatingTo(validator: Validator<T>): Validated<T> =
    object : Validated<T> {
        override val validator: Validator<T>
            get() = validator
    }

fun <T : Any> Validated<T>.asSelfValidated(): SelfValidated?
    = if (validator.supportsClass(this::class)) SelfValidated(this) else null
