package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.testing.testsFor
import io.kotest.core.spec.style.FreeSpec
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.Gen
import io.kotest.property.arbitrary.chunked
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.kotest.property.exhaustive.of

class ContainerTests : FreeSpec({
    testsFor(anyContainer()) {
        takingCaseAsValue {
            check { NotEmpty } shouldPassWhen { value.isNotEmpty() }

            check { Empty } shouldPassWhen { value.isEmpty() }
        }
    }
    testsFor(containersCreatedFrom<String>()) {
        takingCaseAsValue {
            rule { notEmpty() } shouldPassWhen { value.isNotEmpty() } orFail
                    { withMessage("Value must not be empty") }

            rule { empty() } shouldPassWhen { value.isEmpty() } orFail
                    { withMessage("Value must be empty") }
        }
    }
    testsFor(containersCreatedFrom<Collection<Any>>()) {
        takingCaseAsValue {
            rule { notEmpty() } shouldPassWhen { value.isNotEmpty() } orFail
                    { withMessage("Value must not be empty") }

            rule { empty() } shouldPassWhen { value.isEmpty() } orFail
                    { withMessage("Value must be empty") }
        }
    }
    testsFor(containersCreatedFrom<Array<Any>>()) {
        takingCaseAsValue {
            rule { notEmpty() } shouldPassWhen { value.isNotEmpty() } orFail
                    { withMessage("Value must not be empty") }

            rule { empty() } shouldPassWhen { value.isEmpty() } orFail
                    { withMessage("Value must be empty") }
        }
    }
})

private fun anyContainer(): Gen<Container> =
    Exhaustive.of(
        Container { true }, // empty
        Container { false }, // non-empty
    )

@Suppress("UNCHECKED_CAST")
private inline fun <reified T> containersCreatedFrom(): Exhaustive<T> =
    when (T::class) {
        String::class -> Exhaustive.of(emptyString, notEmptyString())
        Collection::class -> Exhaustive.of(emptyCollection, notEmptyCollection())
        Array::class -> Exhaustive.of(emptyArray, notEmptyArray())
        else -> throw IllegalArgumentException(
            "Type ${T::class.qualifiedName} doesn't support ${Container::class.qualifiedName}"
        )
    } as Exhaustive<T>

private val emptyString = ""
private fun notEmptyString() = Arb.string(1..10).next()
private val emptyCollection = emptyList<Any>()
private fun notEmptyCollection() = Arb.constant(Any()).chunked(1..10).next()
private val emptyArray = emptyArray<Any>()
private fun notEmptyArray() = notEmptyCollection().toTypedArray()
