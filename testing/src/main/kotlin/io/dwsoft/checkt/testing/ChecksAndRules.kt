package io.dwsoft.checkt.testing

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.ErrorDetailsBuilder
import io.dwsoft.checkt.core.ErrorDetailsBuilderContext
import io.dwsoft.checkt.core.toValidationRule

val alwaysFailingRule = alwaysFailWithMessage { "$value - ${validationPath()}" }

fun alwaysFailWithMessage(
    buildingBlock: ErrorDetailsBuilderContext<Any?, AlwaysFailingCheck.Key, Check.Params.None>.() -> String
) = AlwaysFailingCheck.toValidationRule(buildingBlock)

fun alwaysFail(
    errorDetailsBuilder: ErrorDetailsBuilder<Any?, AlwaysFailingCheck.Key, Check.Params.None>
) = AlwaysFailingCheck.toValidationRule(errorDetailsBuilder)

object AlwaysFailingCheck : Check<Any?, AlwaysFailingCheck.Key, Check.Params.None> {
    override val context = Check.Context.of(Key, Check.Params.None)

    override fun invoke(value: Any?) = false

    object Key : Check.Key
}

val alwaysPassingRule = AlwaysPassingCheck.toValidationRule { "" }

object AlwaysPassingCheck : Check<Any, AlwaysPassingCheck.Key, Check.Params.None> {
    override val context = Check.Context.of(Key, Check.Params.None)

    override fun invoke(value: Any) = true

    object Key : Check.Key
}
