package io.dwsoft.checkt.core

/**
 * Validation specification represents a function validating value [T] under named scope.
 */
sealed interface ValidationSpec<T> {
    /**
     * Validates given [value] in an optionally [named][namedAs] scope.
     */
    suspend fun validate(value: T, namedAs: NonBlankString? = null): Result<ValidationResult>
}

/**
 * Entry point of defining [validation logic][ValidationSpec] for values of type [T].
 */
fun <T> validationSpec(validationBlock: ValidationBlock<T>): ValidationSpec<T> =
    ValidationSpecInternal(validationBlock)


/**
 * Alias of [ValidationSpec.validate].
 */
suspend operator fun <T> ValidationSpec<T>.invoke(value: T, namedAs: NonBlankString? = null) =
    validate(value, namedAs)

/**
 * "Overloaded" version of [ValidationSpec.invoke].
 */
suspend fun <T> T.validate(namedAs: NonBlankString? = null, with: ValidationSpec<T>) =
    with.validate(this, namedAs)

suspend fun <T> Named<T>.validate(with: ValidationSpec<T>) = with(value, name)

/**
 * Validates given value against [spec][ValidationSpec] created out of passed
 * [block][ValidationBlock].
 */
suspend fun <T> T.validate(namedAs: NonBlankString? = null, block: ValidationBlock<T>) =
    validate(namedAs, validationSpec(block))

private class ValidationSpecInternal<T>(
    private val validationBlock: ValidationBlock<T>
) : ValidationSpec<T> {
    override suspend fun validate(value: T, namedAs: NonBlankString?): Result<ValidationResult> =
        runCatching {
            val scope = namedAs?.let { ValidationScope(it) } ?: ValidationScope()
            Validation(value, scope).apply { validationBlock().getOrThrow() }
            scope.result
        }
}


