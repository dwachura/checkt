package io.dwsoft.checkt.core.check

import io.kotest.core.spec.style.StringSpec

class NonNullTests : StringSpec({
    "non-null value passes check" {
        Any() shouldPass NonNull
    }

    "null value violates check" {
        null shouldNotPass NonNull
    }
})
