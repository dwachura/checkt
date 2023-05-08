package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.testing.testsFor
import io.kotest.core.spec.style.FreeSpec
import io.kotest.property.Exhaustive
import io.kotest.property.Gen
import io.kotest.property.exhaustive.of

class EqualityTests : FreeSpec({
    testsFor(equalityCases()) {
        fromCase(take = { first }) {
            check { Equals(case.second) } shouldPassWhen { value == case.second }

            check { IsDifferent(case.second) } shouldPassWhen { value != case.second }

            rule { equalTo(case.second) } shouldPassWhen { value == case.second } orFail
                    { withMessage("Value must equal to ${case.second}") }

            rule { differentThan(case.second) } shouldPassWhen { value != case.second } orFail
                    { withMessage("Value must be different than ${case.second}") }
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
