package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.testing.testsFor
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Exhaustive
import io.kotest.property.Gen
import io.kotest.property.exhaustive.of

class EqualityTests : FreeSpec({
    testsFor<Equals<Any?>, _, _>(
        runFor = equalityCases(),
        checking = { first },
        validWhen = { it == second },
        check = { Equals(second) },
        rule = { equalTo(it.second) },
        violationMessage = { it shouldContain "Value must equal to $second" }
    )

    testsFor<IsDifferent<Any?>, _, _>(
        runFor = equalityCases(),
        checking = { first },
        validWhen = { it != second },
        check = { IsDifferent(second) },
        rule = { differentThan(it.second) },
        violationMessage = { it shouldContain "Value must be different than $second" }
    )
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
