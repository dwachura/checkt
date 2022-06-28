package io.dwsoft.checkt.core.check

import io.dwsoft.checkt.core.check.Check.Context

object NonNull : Check<Any?, NonNull.Key, NonNull.Params> {
    override val context: Context<Key, Params> = Context.of(Key, Params)

    override fun invoke(value: Any?): Boolean = value != null

    object Key : Check.Key
    object Params : Check.Params.None()
}
