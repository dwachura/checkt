package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.Check.Params.None
import kotlin.reflect.KClass

interface Check<in V, P : Check.Params<SELF>, SELF : Check<V, P, SELF>> {
    val params: P

    operator fun invoke(value: V): Boolean

    /**
     * Unique key for [Check] type.
     */
    data class Key<C : Check<*, *, *>>(val checkClass: KClass<out C>) {
        val shortIdentifier: String = checkClass.java.simpleName
        val fullIdentifier: String = checkClass.java.canonicalName
    }

    abstract class Params<C : Check<*, *, C>> protected constructor() {
        class None<C : Check<*, None<C>, C>> : Params<C>() {
            override fun toString(): String = "Check.Params.None"

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false
                return true
            }

            override fun hashCode(): Int {
                return javaClass.hashCode()
            }
        }
    }

    interface WithoutParams<V, SELF : Check<V, None<SELF>, SELF>> : Check<V, None<SELF>, SELF> {
        companion object {
            inline fun <C : Check<V, None<C>, C>, V> delegate(
                crossinline implementation: (value: V) -> Boolean
            ): WithoutParams<V, C> =
                object : WithoutParams<V, C> {
                    override val params: None<C> = None()

                    override fun invoke(value: V): Boolean = implementation(value)
                }
        }
    }
}

fun <C : Check<*, *, *>> KClass<out C>.checkKey(): Check.Key<C> = Check.Key(this)

val <C : Check<*, *, *>> C.key: Check.Key<C>
    get() = this::class.checkKey()
