package io.dwsoft.checkt.core

import kotlin.reflect.KProperty0

fun <T> validate(
    value: T,
    namedAs: Displayed? = null,
    validation: context(ValidationScope, ValidationDsl) T.() -> Unit,
): ValidationScope =
    ValidationScope(namedAs).apply { validation(ValidationDsl, value) }

object ValidationDsl {
    context(ValidationScope)
    operator fun <T> T.invoke(
        namedAs: Displayed,
        validation: context(ValidationScope, ValidationDsl) T.() -> Unit,
    ): ValidationScope =
        enclose(namedAs).apply { validation(ValidationDsl, this@invoke) }

    context(ValidationScope)
    operator fun <T> KProperty0<T>.invoke(
        validation: context(ValidationScope, ValidationDsl) T.() -> Unit,
    ): ValidationScope =
        get()(namedAs = name.toDisplayed(), validation)

    context(ValidationScope)
    infix fun <V : Any?, K : Check.Key, P : Check.Params> V.must(
        rule: ValidationRule<V, K, P>
    ): ValidationError<V, K, P>? =
        checkAgainst(rule)
}
