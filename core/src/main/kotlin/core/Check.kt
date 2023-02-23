package io.dwsoft.checkt.core

import kotlin.reflect.KClass

interface Check<in V> {
    suspend operator fun invoke(value: V): Boolean

    /**
     * Unique key for [Check] type.
     */
    data class Key<C : Check<*>>(val checkClass: KClass<out C>) {
        val shortIdentifier: String = checkClass.java.simpleName
        val fullIdentifier: String = checkClass.java.canonicalName
    }

    companion object {
        fun <C : Check<V>, V> delegate(
            implementation: Check<V>.(value: V) -> Boolean
        ): Check<V> =
            object : Check<V> {
                override suspend fun invoke(value: V): Boolean =
                    implementation(value)
            }
    }
}

fun <C : Check<*>> KClass<out C>.checkKey(): Check.Key<C> =
    Check.Key(this)

val <C : Check<*>> C.key: Check.Key<C>
    get() = this::class.checkKey()

interface ParamsOf<T : ParameterizedCheck<*, SELF>, SELF : ParamsOf<T, SELF>>

interface ParameterizedCheck<in V, P : ParamsOf<*, P>> : Check<V> {
    val params: P
}
