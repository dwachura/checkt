package io.dwsoft.checkt.validator

import io.dwsoft.checkt.core.NonBlankString
import io.dwsoft.checkt.core.ValidationSpecification
import io.dwsoft.checkt.core.ValidationStatus
import kotlin.reflect.KClass

inline fun <reified T : Any> ValidationSpecification<T>.asValidator(): Validator<T> =
    Validator(this)

class Validator<V : Any> private constructor(
    val validationSpecification: ValidationSpecification<V>,
    internal val supportedType: KClass<V>,
) {
    suspend fun validate(value: V, name: NonBlankString? = null): ValidationStatus {
        val valueStatus = validationSpecification(value, name)
        return valueStatus
    }

    companion object {
        inline operator fun <reified T : Any> invoke(
            noinline validationSpecification: ValidationSpecification<T>
        ) = createFor(T::class, validationSpecification)

        fun <T : Any> createFor(
            type: KClass<T>,
            validationSpecification: ValidationSpecification<T>
        ): Validator<T> =
            Validator(validationSpecification, type)
    }
}

internal fun <T : Any> Validator<*>.supportsValue(value: T): Boolean =
    supportedType.isInstance(value)

//fun <V : Validated<V>> V.validate(): ValidationResult = validator.validate(this)
//
//fun <V : Validated<V>> validationSpec(
//    validation: ValidationBlock<V>
//): Validated<V> =
//    object : Validated<V> {
//        override val validator: Validator<V> =
//            object : Validator<V> {
//                override fun validate(value: V): ValidationResult =
//                    validate(value, namedAs = null, validation = validation).getOrThrow()
//            }
//    }
