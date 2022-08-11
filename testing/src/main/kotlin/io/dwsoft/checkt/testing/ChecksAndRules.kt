package io.dwsoft.checkt.testing

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.ErrorDetailsBuilder
import io.dwsoft.checkt.core.errorMessage
import io.dwsoft.checkt.core.toValidationRule

fun alwaysFailingRule(
    errorDetailsBuilder: ErrorDetailsBuilder<Any, AlwaysFailingCheck.Key, Check.Params.None>
) = AlwaysFailingCheck.toValidationRule(errorDetailsBuilder)

object AlwaysFailingCheck : Check<Any, AlwaysFailingCheck.Key, Check.Params.None> {
    override val context = Check.Context.of(Key, Check.Params.None)

    override fun invoke(value: Any) = false

    object Key : Check.Key
}

val alwaysPassingRule = AlwaysPassingCheck.toValidationRule(errorMessage { "" })

object AlwaysPassingCheck : Check<Any, AlwaysPassingCheck.Key, Check.Params.None> {
    override val context = Check.Context.of(Key, Check.Params.None)

    override fun invoke(value: Any) = true

    object Key : Check.Key
}
