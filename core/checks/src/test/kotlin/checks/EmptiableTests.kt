package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.testing.testCheck
import io.dwsoft.checkt.testing.testRule
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.Gen
import io.kotest.property.arbitrary.chunked
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.kotest.property.exhaustive.map
import io.kotest.property.exhaustive.of
import io.kotest.property.exhaustive.plus

class EmptiableTests : FreeSpec({
    "${NotEmpty::class.simpleName}" - {
        testCheck<NotEmpty, _, _>(
            runFor = allEmptiableCases(),
            checking = { this },
            validWhen = { it.isNotEmpty() },
            check = { NotEmpty },
        )

        testRule<NotEmpty, _, _>(
            runFor = emptiableCases<String>(),
            checking = { this },
            validWhen = { it.isNotEmpty() },
            rule = { notEmpty() },
            violationMessage = { it shouldContain "Value must be empty" }
        )

        testRule<NotEmpty, _, _>(
            runFor = emptiableCases<Collection<Any>>(),
            checking = { this },
            validWhen = { it.isNotEmpty() },
            rule = { notEmpty() },
            violationMessage = { it shouldContain "Collection must be empty" }
        )

        testRule<NotEmpty, _, _>(
            runFor = emptiableCases<Array<Any>>(),
            checking = { this },
            validWhen = { it.isNotEmpty() },
            rule = { notEmpty() },
            violationMessage = { it shouldContain "Array must be empty" }
        )
    }

    "${Empty::class.simpleName}" - {
        testCheck<Empty, _, _>(
            runFor = allEmptiableCases(),
            checking = { this },
            validWhen = { it.isEmpty() },
            check = { Empty },
        )

        testRule<Empty, _, _>(
            runFor = emptiableCases<String>(),
            checking = { this },
            validWhen = { it.isEmpty() },
            rule = { empty() },
            violationMessage = { it shouldContain "Value must not be empty" }
        )

        testRule<Empty, _, _>(
            runFor = emptiableCases<Collection<Any>>(),
            checking = { this },
            validWhen = { it.isEmpty() },
            rule = { empty() },
            violationMessage = { it shouldContain "Collection must not be empty" }
        )

        testRule<Empty, _, _>(
            runFor = emptiableCases<Array<Any>>(),
            checking = { this },
            validWhen = { it.isEmpty() },
            rule = { empty() },
            violationMessage = { it shouldContain "Array must not be empty" }
        )
    }

})

private fun allEmptiableCases(): Gen<Emptiable> =
    emptiableCases<String>().map { Emptiable.of(it) } +
            emptiableCases<Collection<*>>().map { Emptiable.of(it) } +
            emptiableCases<Array<*>>().map { Emptiable.of(it) }

private inline fun <reified T> emptiableCases(): Exhaustive<T> =
    when (T::class) {
        String::class -> Exhaustive.of(emptyString, notEmptyString())
        Collection::class -> Exhaustive.of(emptyCollection, notEmptyCollection())
        Array::class -> Exhaustive.of(emptyArray, notEmptyArray())
        else -> throw IllegalArgumentException(
            "Type ${T::class.qualifiedName} doesn't support ${Emptiable::class.qualifiedName}"
        )
    } as Exhaustive<T>

private val emptyString = ""
private fun notEmptyString() = Arb.string(1..10).next()
private val emptyCollection = emptyList<Any>()
private fun notEmptyCollection() = Arb.constant(Any()).chunked(1..10).next()
private val emptyArray = emptyArray<Any>()
private fun notEmptyArray() = notEmptyCollection().toTypedArray()
