package io.dwsoft.checkt.core.check

import io.dwsoft.checkt.core.check.Check.Key
import io.dwsoft.checkt.core.check.Check.Params

interface Check<in V, K : Key, P : Params> {
    val context: Context<K, P>

    operator fun invoke(value: V): Boolean

    /**
     * Unique key for [Check] type. Typically, implemented as an object.
     */
    interface Key

    abstract class Params protected constructor() {
        abstract class None : Params() {
            override fun toString(): String = "NONE"
        }
    }

    data class Context<K : Key, P : Params>(val key: K, val params: P) {
        companion object {
            fun <K : Key, P : Params> of(key: K, params: P): Context<K, P> =
                Context(key, params)
        }
    }
}

class InvertedCheck<V, K : Key, P : Params>(val check: Check<V, K, P>) :
    Check<V, InvertedCheck.Key<K>, P>
{
    override val context: Check.Context<Key<K>, P> =
        with(check.context) {
            Check.Context.of(Key(key), params)
        }

    override fun invoke(value: V): Boolean = !check(value)

    data class Key<K>(val originalKey: K) : Check.Key
}

/**
 * Inverts [check][this].
 *
 * @throws IllegalArgumentException when check is already [inverted][InvertedCheck]
 */
fun <V, K : Key, P : Params> Check<V, K, P>.invert(): InvertedCheck<V, K, P> =
    when (this) {
        is InvertedCheck<*, *, *> ->
            throw IllegalArgumentException("Check (key: ${context.key}) is already inverted")
        else -> InvertedCheck(this)
    }
