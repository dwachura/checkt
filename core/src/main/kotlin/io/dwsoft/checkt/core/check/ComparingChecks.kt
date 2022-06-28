package io.dwsoft.checkt.core.check

import io.dwsoft.checkt.core.check.Check.Context

class LessThan<V : Comparable<V>>(private val max: V) :
    Check<V, LessThan.Key, LessThan.Params<V>>
{
    override val context: Context<Key, Params<V>> = Context.of(Key, Params(max))

    override fun invoke(value: V): Boolean = value < max

    object Key : Check.Key
    data class Params<V>(val max: V) : Check.Params()
}

class LessThanOrEqual<V : Comparable<V>>(private val max: V) :
    Check<V, LessThanOrEqual.Key, LessThanOrEqual.Params<V>>
{
    override val context: Context<Key, Params<V>> = Context.of(Key, Params(max))

    override fun invoke(value: V): Boolean = value <= max

    object Key : Check.Key
    data class Params<V>(val max: V) : Check.Params()
}

class GreaterThan<V : Comparable<V>>(private val min: V) :
    Check<V, GreaterThan.Key, GreaterThan.Params<V>>
{
    override val context: Context<Key, Params<V>> = Context.of(Key, Params(min))

    override fun invoke(value: V): Boolean = value > min

    object Key : Check.Key
    data class Params<V>(val min: V) : Check.Params()
}

class GreaterThanOrEqual<V : Comparable<V>>(private val min: V) :
    Check<V, GreaterThanOrEqual.Key, GreaterThanOrEqual.Params<V>>
{
    override val context: Context<Key, Params<V>> = Context.of(Key, Params(min))

    override fun invoke(value: V): Boolean = value >= min

    object Key : Check.Key
    data class Params<V>(val min: V) : Check.Params()
}
