package io.dwsoft.checkt.core.validation.dsl

import io.dwsoft.checkt.core.check.Check
import io.dwsoft.checkt.core.check.InvertedCheck
import io.dwsoft.checkt.core.check.invert
import io.dwsoft.checkt.core.validation.Displayed
import io.dwsoft.checkt.core.validation.NamingScope
import io.dwsoft.checkt.core.validation.ValidationError
import io.dwsoft.checkt.core.validation.validationPath

sealed interface ValidationRule<V, K : Check.Key, P : Check.Params> {
    val check: Check<V, K, P>
    val errorDetailsBuilder: ErrorDetailsBuilder<V, K, P>

    companion object {
        fun <V, K : Check.Key, P : Check.Params> of(
            check: Check<V, K, P>,
            errorDetailsBuilder: ErrorDetailsBuilder<V, K, P>
        ): ValidationRule<V, K, P> =
            Standard(check, errorDetailsBuilder)

        fun <V, K : Check.Key, P : Check.Params> of(
            check: InvertedCheck<V, K, P>,
            errorDetailsBuilder: ErrorDetailsBuilder<V, InvertedCheck.Key<K>, P>
        ): ValidationRule<V, InvertedCheck.Key<K>, P> =
            Inverted(check, errorDetailsBuilder)
    }
}

private class Standard<V, K : Check.Key, P : Check.Params>(
    override val check: Check<V, K, P>,
    override val errorDetailsBuilder: ErrorDetailsBuilder<V, K, P>
) : ValidationRule<V, K, P>

private class Inverted<V, K : Check.Key, P : Check.Params>(
    override val check: InvertedCheck<V, K, P>,
    override val errorDetailsBuilder: ErrorDetailsBuilder<V, InvertedCheck.Key<K>, P>
) : ValidationRule<V, InvertedCheck.Key<K>, P>

fun <V, K : Check.Key, P : Check.Params> ValidationRule<V, K, P>.invert(
    errorDetailsBuilder: ErrorDetailsBuilder<V, InvertedCheck.Key<K>, P>
): ValidationRule<V, InvertedCheck.Key<K>, P> =
    ValidationRule.of(check.invert(), errorDetailsBuilder)

data class ErrorDetailsBuilderContext<V, K : Check.Key, P : Check.Params> internal constructor(
    val value: V,
    val namingScope: NamingScope,
    private val failedCheck: Check.Context<K, P>,
) {
    val validationParams: P
        get() = failedCheck.params

    fun validationPath(separator: String = "."): String = namingScope.validationPath(separator)
}

typealias ErrorDetailsBuilder<V, K, P> = ErrorDetailsBuilderContext<V, K, P>.() -> Displayed
