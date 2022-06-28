package io.dwsoft.checkt.core.check

import io.dwsoft.checkt.core.check.Check.Context

class Equals<V>(private val other: V) : Check<V, Equals.Key, Equals.Params<V>> {
    override val context: Context<Key, Params<V>> = Context.of(Key, Params(other))

    override fun invoke(value: V): Boolean = value == other

    object Key : Check.Key
    data class Params<V>(val other: V) : Check.Params()
}
