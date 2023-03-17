package io.dwsoft.checkt.core

import kotlin.reflect.KClass

interface Check<in V> {
    suspend operator fun invoke(value: V): Result

    sealed interface Result {
        val passed: Boolean

        companion object {
            operator fun invoke(passed: Boolean): Result = Impl(passed)

            @JvmInline
            private value class Impl(override val passed: Boolean) : Result
        }
    }

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
                override suspend fun invoke(value: V): Result =
                    Result(implementation(value))
            }
    }
}

fun <C : Check<*>> KClass<out C>.checkKey(): Check.Key<C> =
    Check.Key(this)

val <C : Check<*>> C.key: Check.Key<C>
    get() = this::class.checkKey()

interface ParamsOf<T : ParameterizedCheck<*, SELF>, SELF : ParamsOf<T, SELF>>

interface ParameterizedCheck<in V, P : ParamsOf<*, P>> : Check<V> {
    override suspend fun invoke(value: V): Result<P>

    class Result<P : ParamsOf<*, P>>(
        override val passed: Boolean,
        val params: P
    ) : Check.Result
}

infix fun <P : ParamsOf<out ParameterizedCheck<V, P>, P>, V> P.and(
    implementation: P.(value: V) -> Boolean
): ParameterizedCheck<V, P> =
    object : ParameterizedCheck<V, P> {
        private val params: P = this@and

        override suspend fun invoke(value: V): ParameterizedCheck.Result<P> =
            ParameterizedCheck.Result(params.implementation(value), params)
    }
