package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.testing.testsFor
import io.kotest.core.spec.style.FreeSpec
import io.kotest.property.Exhaustive
import io.kotest.property.Gen
import io.kotest.property.exhaustive.of

class BooleansTests : FreeSpec({
    testsFor(booleanCases()) {
        takingCaseAsValue {
            rule { beTrue } shouldPassWhen { value.isTrue() } orFail {
                withMessage("Value must be true")
            }

            rule { notBeTrue } shouldPassWhen { value == null || value.isFalse() } orFail {
                withMessage("Value must not be true")
            }

            rule { beFalse } shouldPassWhen { value.isFalse() } orFail {
                withMessage("Value must be false")
            }

            rule { notBeFalse } shouldPassWhen { value == null || value.isTrue() } orFail {
                withMessage("Value must not be false")
            }
        }
    }
})

private fun booleanCases(): Gen<Boolean?> =
    Exhaustive.of(null, true, false)

private fun Boolean?.isTrue(): Boolean = this != null && this

private fun Boolean?.isFalse(): Boolean = this != null && !this
