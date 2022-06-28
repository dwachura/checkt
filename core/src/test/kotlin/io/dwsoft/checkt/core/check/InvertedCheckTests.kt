package io.dwsoft.checkt.core.check

import io.dwsoft.checkt.core.check.Check.Context
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf

class InvertedCheckTests : StringSpec({
    "test check works" {
        Any() shouldPass TestCheck()
    }

    "test check is inverted" {
        val invertedCheck = TestCheck().invert()

        Any() shouldNotPass invertedCheck
        with(invertedCheck.context) {
            key.shouldBeInstanceOf<InvertedCheck.Key<*>>()
                .originalKey.shouldBeInstanceOf<TestCheck.Key>()
            params.shouldBeInstanceOf<TestCheck.Params>()
        }
    }

    "inverted check cannot be inverted again" {
        val invertedCheck = TestCheck().invert()

        shouldThrow<IllegalArgumentException> { invertedCheck.invert() }
            .message shouldContain "Check \\(key: .+\\) is already inverted".toRegex()
    }
})

private class TestCheck : Check<Any, TestCheck.Key, TestCheck.Params> {
    override val context: Context<Key, Params> = Context.of(Key, Params)

    override fun invoke(value: Any): Boolean = true

    object Key : Check.Key
    object Params : Check.Params()
}
