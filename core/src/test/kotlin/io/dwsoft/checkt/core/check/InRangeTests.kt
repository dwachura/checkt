package io.dwsoft.checkt.core.check

import io.kotest.core.spec.style.StringSpec

class InRangeTests : StringSpec({
    "value in range passes check" {
        6 shouldPass InRange(5..10)
    }

    "value outside range violates check" {
        'p' shouldNotPass InRange('a'..'e')
    }
})
