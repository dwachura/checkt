package io.dwsoft.checkt.core.check

import io.dwsoft.checkt.core.check.Check.Context

class InRange<V : Comparable<V>>(private val range: ClosedRange<V>) :
    Check<V, InRange.Key, InRange.Params<V>>
{
    override val context: Context<Key, Params<V>> = Context.of(Key, Params(range))

    override fun invoke(value: V): Boolean = value in range

    object Key : Check.Key
    data class Params<V : Comparable<V>>(val range: ClosedRange<V>) : Check.Params()
}
