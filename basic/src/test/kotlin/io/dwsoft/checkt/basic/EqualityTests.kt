package io.dwsoft.checkt.basic

import io.dwsoft.checkt.testing.forAll
import io.dwsoft.checkt.testing.shouldNotPass
import io.dwsoft.checkt.testing.shouldPass
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Exhaustive
import io.kotest.property.Gen
import io.kotest.property.exhaustive.of

class EqualityTests : StringSpec({
    "${Equals::class.simpleName} check" {
        forAll(equalityCases()) {
            when {
                first != second -> first shouldNotPass Equals(second)
                else -> first shouldPass Equals(second)
            }
        }
    }

    "${IsDifferent::class.simpleName} check" {
        forAll(equalityCases()) {
            when {
                first != second -> first shouldPass IsDifferent(second)
                else -> first shouldNotPass IsDifferent(second)
            }
        }
    }
})

private fun equalityCases(): Gen<Pair<Any?, Any?>> {
    val any = Any()
    return Exhaustive.of(
        Pair(any, any), // equal values
        Pair(any, Any()), // different values
        Pair(any, null), // right value null
        Pair(null, any), // left value null
        Pair(null, null), // both null
    )
}
