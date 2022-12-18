package io.dwsoft.checkt.testing

import io.dwsoft.checkt.core.NotBlankString
import io.kotest.property.Arb
import io.kotest.property.Gen
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.ascii
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

suspend fun <T> forAll(cases: Gen<T>, verify: suspend T.() -> Unit) {
    checkAll(cases) { verify(it) }
}

fun Arb.Companion.notBlankString(
    minSize: Int = 0,
    maxSize: Int = 100,
    codepoint: Arb<Codepoint> = Codepoint.ascii()
): Arb<NotBlankString> =
    arbitrary {
        string(minSize, maxSize, codepoint)
            .filter { it.isNotBlank() }
            .map { NotBlankString(it) }
            .bind()
    }
