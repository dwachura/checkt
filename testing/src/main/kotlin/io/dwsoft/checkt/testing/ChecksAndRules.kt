package io.dwsoft.checkt.testing

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorDetails
import io.dwsoft.checkt.core.toValidationRule

val alwaysFailingRule = alwaysFailWithMessage { "$value - ${validationPath()}" }

fun alwaysFailWithMessage(
    errorDetails: LazyErrorDetails<AlwaysFailingCheck, Any?, Check.Params.None<AlwaysFailingCheck>>
) = AlwaysFailingCheck.toValidationRule(errorDetails)

object AlwaysFailingCheck : Check.WithoutParams<Any?, AlwaysFailingCheck> by Check.WithoutParams.delegate(
    implementation = { false }
)

val alwaysPassingRule = AlwaysPassingCheck.toValidationRule { "" }

object AlwaysPassingCheck : Check.WithoutParams<Any, AlwaysPassingCheck> by Check.WithoutParams.delegate(
    implementation = { true }
)
