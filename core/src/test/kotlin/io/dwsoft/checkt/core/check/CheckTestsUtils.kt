package io.dwsoft.checkt.core.check

import io.kotest.matchers.shouldBe

infix fun <V> V.shouldPass(check: Check<V, *, *>) = check(this) shouldBe true

infix fun <V> V.shouldNotPass(check: Check<V, *, *>) = check(this) shouldBe false
