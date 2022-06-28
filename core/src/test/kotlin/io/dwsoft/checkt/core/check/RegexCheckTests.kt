package io.dwsoft.checkt.core.check

import io.kotest.core.spec.style.StringSpec

class RegexCheckTests : StringSpec({
    val regex = "\\d{3}[a-z]{3}".toRegex()

    "valid-formatted value passes check" {
        "123abc" shouldPass RegexCheck(regex)
    }

    "invalid-formatted value violates check" {
        "aaa" shouldNotPass RegexCheck(regex)
    }
})
