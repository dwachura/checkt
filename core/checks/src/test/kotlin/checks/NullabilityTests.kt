package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.testing.forAll
import io.dwsoft.checkt.testing.shouldNotPass
import io.dwsoft.checkt.testing.shouldPass
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Exhaustive
import io.kotest.property.Gen
import io.kotest.property.exhaustive.of

class NullabilityTests : StringSpec({
    "${NonNull::class.simpleName} check" {
        forAll(nullabilityCases()) {
            when {
                value != null -> value shouldPass NonNull()
                else -> value shouldNotPass NonNull()
            }
        }
    }

    "${IsNull::class.simpleName} check" {
        forAll(nullabilityCases()) {
            when {
                value != null -> value shouldNotPass IsNull()
                else -> value shouldPass IsNull()
            }
        }
    }
})

private fun nullabilityCases(): Gen<NullabilityCase> =
    Exhaustive.of(
        nonNullValue(),
        nullValue()
    )

private fun nonNullValue(): NullabilityCase =
    NullabilityCase(Any())

private fun nullValue(): NullabilityCase =
    NullabilityCase(null)

private data class NullabilityCase(val value: Any?)

