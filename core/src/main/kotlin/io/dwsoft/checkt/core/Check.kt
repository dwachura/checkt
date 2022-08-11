package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.Check.Key
import io.dwsoft.checkt.core.Check.Params

interface Check<in V, K : Key, P : Params> {
    val context: Context<K, P>

    operator fun invoke(value: V): Boolean

    /**
     * Unique key for [Check] type.
     */
    interface Key

    abstract class Params protected constructor() {
        object None : Params() {
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
