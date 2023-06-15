package io.dwsoft.checkt.core

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

    companion object {
        operator fun <C : Check<V>, V> invoke(implementation: (value: V) -> Boolean): Check<V> =
            object : Check<V> {
                override suspend fun invoke(value: V): Result =
                    Result(implementation(value))
            }
    }
}

val Check<*>.key: String
    get() = this::class.java.canonicalName

val Check<*>.name: String
    get() = this::class.java.simpleName

interface ParamsOf<T : ParameterizedCheck<*, SELF>, SELF : ParamsOf<T, SELF>>

interface ParameterizedCheck<in V, P : ParamsOf<*, P>> : Check<V> {
    override suspend fun invoke(value: V): Result<P>

    class Result<P : ParamsOf<*, P>>(override val passed: Boolean, val params: P) : Check.Result
}

infix fun <P : ParamsOf<out ParameterizedCheck<V, P>, P>, V> P.and(
    implementation: P.(value: V) -> Boolean
): ParameterizedCheck<V, P> =
    object : ParameterizedCheck<V, P> {
        private val params: P = this@and

        override suspend fun invoke(value: V): ParameterizedCheck.Result<P> =
            ParameterizedCheck.Result(params.implementation(value), params)
    }
