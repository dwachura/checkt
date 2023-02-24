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
        operator fun <C : Check<V>, V> invoke(
            implementation: (value: V) -> Boolean
        ): Check<V> =
            object : Check<V> {
                override suspend fun invoke(value: V) = implementation(value)
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

infix fun <P : ParamsOf<out ParameterizedCheck<V, P>, P>, V> P.and(
    implementation: P.(value: V) -> Boolean
): ParameterizedCheck<V, P> =
    object : ParameterizedCheck<V, P> {
        override val params: P = this@and

        override suspend fun invoke(value: V) = params.implementation(value)
    }
