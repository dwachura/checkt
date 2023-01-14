package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.Check.Params.None
import kotlin.reflect.KClass

/*
 * TODO: "dynamic check" - check that can be defined adhoc with some "not-static" key
 *  (to differentiate "kinds" of such check) and condition.
 *  Params would be "unsafe" too - generic map; creator/user would be responsible to
 *  deal with them (parsing, usage, displaying).
 *
 * class DynamicCheck : Check<Any, DynamicCheck.Params, DynamicCheck>(
 *      id
 *      predicate,
 *      params: Map<String, Any>,
 * ) {
 *  key = class + id
 * }
 */

interface Check<in V, P : Check.Params<SELF>, SELF : Check<V, P, SELF>> {
    val params: P

    suspend operator fun invoke(value: V): Boolean

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

    interface Parameterless<V, SELF : Check<V, None<SELF>, SELF>> :
        Check<V, None<SELF>, SELF>
    {
        companion object {
            fun <C : Check<V, None<C>, C>, V> delegate(
                implementation: (value: V) -> Boolean
            ): Parameterless<V, C> =
                object : Parameterless<V, C> {
                    override val params: None<C> = None()

                    override suspend fun invoke(value: V): Boolean =
                        implementation(value)
                }
        }
    }
}

fun <C : Check<*, *, *>> KClass<out C>.checkKey(): Check.Key<C> =
    Check.Key(this)

val <C : Check<*, *, *>> C.key: Check.Key<C>
    get() = this::class.checkKey()
