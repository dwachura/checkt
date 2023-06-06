package io.dwsoft.checkt.core.checks.numeric

import io.dwsoft.checkt.core.checks.beNegative
import io.dwsoft.checkt.core.checks.bePositive
import io.dwsoft.checkt.core.checks.notBeNegative
import io.dwsoft.checkt.core.checks.notBePositive
import io.dwsoft.checkt.testing.testsFor
import io.kotest.core.spec.style.FreeSpec
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.Gen
import io.kotest.property.arbitrary.byte
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.short
import io.kotest.property.exhaustive.of

class NumberSignTests : FreeSpec({
    testsFor(numberCases()) {
        takingCaseAsValue {
            rule { bePositive() } shouldPassWhen { value.toDouble() > 0.0 } orFail {
                withMessage("Number must be positive")
            }

            rule { notBePositive() } shouldPassWhen { value.toDouble() <= 0.0 } orFail {
                withMessage("Number must not be positive")
            }

            rule { beNegative() } shouldPassWhen { value.toDouble() < 0.0 } orFail {
                withMessage("Number must be negative")
            }

            rule { notBeNegative() } shouldPassWhen { value.toDouble() >= 0.0 } orFail {
                withMessage("Number must not be negative")
            }
        }
    }
})

private fun numberCases(): Gen<Number> =
    Exhaustive.of(
        Arb.double(max = -0.1).next(),
        Arb.double(min = 0.1).next(),
        Arb.float(max = -0.1f).next(),
        Arb.float(min = 0.1f).next(),
        Arb.long(max = -1L).next(),
        Arb.long(min = 1L).next(),
        Arb.int(max = -1).next(),
        Arb.int(min = 1).next(),
        Arb.short(max = -1).next(),
        Arb.short(min = 1).next(),
        Arb.byte(max = -1).next(),
        Arb.byte(min = 1).next(),
        0.0,
        0.0f,
        0L,
        0,
    )
