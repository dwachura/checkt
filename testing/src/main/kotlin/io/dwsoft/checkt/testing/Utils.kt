package io.dwsoft.checkt.testing

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.ValidationFailure
import io.dwsoft.checkt.core.errorMessages
import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.property.Gen
import io.kotest.property.checkAll

infix fun <V> V.shouldPass(check: Check<V, *, *>) =
    "Value '$this' should pass check ${check.context}".asClue {
        check(this) shouldBe true
    }

infix fun <V> V.shouldNotPass(check: Check<V, *, *>) =
    "Value '$this' should not pass check ${check.context}".asClue {
        check(this) shouldBe false
    }

suspend fun <T> forAll(cases: Gen<T>, verify: T.() -> Unit) {
    checkAll(cases) { verify(it) }
}

fun ValidationFailure.shouldContainMessages(vararg expected: String) {
    val errorMessages = errorMessages()
    "Failure messages ($errorMessages) should contain passed error messages".asClue {
        errorMessages.shouldContainAll(expected.toList())
    }
}
