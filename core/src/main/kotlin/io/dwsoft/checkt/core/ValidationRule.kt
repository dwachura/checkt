package io.dwsoft.checkt.core

class ValidationRule<V, K : Check.Key, P : Check.Params>(
    val check: Check<V, K, P>,
    val errorDetailsBuilder: ErrorDetailsBuilder<V, K, P>,
) {
    val context: Check.Context<K, P>
        get() = check.context
}

class ErrorDetailsBuilderContext<V, K : Check.Key, P : Check.Params> internal constructor(
    val value: V,
    val validationPath: NamingPath,
    private val violatedCheck: Check.Context<K, P>,
) {
    val validationParams: P
        get() = violatedCheck.params

    operator fun NamingPath.invoke(
        separator: String = ".",
        transformer: (Displayed) -> String = { it.value },
    ): String =
        validationPath.fold(
            transformingSegments = transformer,
            joiningSegments = { acc, str -> "$acc$separator$str" }
        )
}

typealias ErrorDetailsBuilder<V, K, P> = ErrorDetailsBuilderContext<V, K, P>.() -> Displayed

fun <V, K : Check.Key, P : Check.Params> Check<V, K, P>.toValidationRule(
    errorDetailsBuilder: ErrorDetailsBuilder<V, K, P>
): ValidationRule<V, K, P>
    = ValidationRule(this, errorDetailsBuilder)

fun <V, K : Check.Key, P : Check.Params> errorMessage(
    block: ErrorDetailsBuilderContext<V, K, P>.() -> String
): ErrorDetailsBuilder<V, K, P> =
    { block(this).toDisplayed() }
