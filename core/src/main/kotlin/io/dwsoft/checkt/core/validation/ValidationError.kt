package io.dwsoft.checkt.core.validation

import io.dwsoft.checkt.core.check.Check

data class ValidationError<V, K : Check.Key, P : Check.Params>(
    val value: V,
    val namingScope: NamingScope,
    val failedCheck: Check.Context<K, P>,
    val errorDetails: Displayed,
)
