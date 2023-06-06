package io.dwsoft.checkt.core.checks.textual

import io.dwsoft.checkt.core.checks.notBlank
import io.dwsoft.checkt.testing.testsFor
import io.kotest.core.spec.style.FreeSpec
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.Gen
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.exhaustive.of

class NotBlankTests : FreeSpec({
    testsFor(blankTextCases()) {
        takingCaseAsValue {
            rule { notBlank() } shouldPassWhen { value.isNotBlank() } orFail {
                withMessage("Value must not be blank")
            }
        }
    }
})

private fun blankTextCases(): Gen<CharSequence> =
    Exhaustive.of(
        "",
        Arb.stringPattern("\\s+").next(),
        Arb.stringPattern("\\s*\\S+\\s*").next(),
    )
