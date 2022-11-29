package io.dwsoft.checkt.core

interface Validator<V> {
    fun validate(value: V): ValidationResult
}

interface Validated<V> {
    val validator: Validator<V>
}

fun <V : Validated<V>> V.validate(): ValidationResult = validator.validate(this)

fun <V : Validated<V>> validationSpec(
    validation: ValidationSpec<V>.() -> Unit
): Validated<V> =
    object : Validated<V> {
        override val validator: Validator<V> =
            object : Validator<V> {
                override fun validate(value: V): ValidationResult =
                    validate(value, namedAs = null, validation = validation)
            }
    }
