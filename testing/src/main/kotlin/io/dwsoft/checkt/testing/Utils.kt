package io.dwsoft.checkt.testing

import io.kotest.property.Gen
import io.kotest.property.checkAll

suspend fun <T> forAll(cases: Gen<T>, verify: T.() -> Unit) {
    checkAll(cases) { verify(it) }
}
