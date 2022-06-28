package io.dwsoft.checkt.core.check

import io.kotest.core.spec.style.StringSpec
import io.kotest.core.tuple
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table

class EqualsTests : StringSpec({
    "equal value passes check" {
        forAll(
            table(
                headers("value", "should be equal to"),
                row("abcd", "abcd"),
                row(1234, 1234),
                row(tuple("abc", 123), tuple("abc", 123)),
                row(null, null)
            )
        ) { value: Any?, other: Any? ->
            value shouldPass Equals(other)
        }
    }

    "non-equal value violates check" {
        forAll(
            table(
                headers("value", "different value"),
                row("zzzzzz", "abcd"),
                row(1111, 1234),
                row(tuple("xxx", 111), tuple("abc", 123)),
                row(null, Any())
            )
        ) { value: Any?, other: Any? ->
            value shouldNotPass Equals(other)
        }
    }
})
