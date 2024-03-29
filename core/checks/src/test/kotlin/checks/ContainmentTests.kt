package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.validation
import io.dwsoft.checkt.testing.forAll
import io.dwsoft.checkt.testing.shouldBeInvalidBecause
import io.dwsoft.checkt.testing.shouldBeValid
import io.dwsoft.checkt.testing.shouldNotPass
import io.dwsoft.checkt.testing.shouldPass
import io.dwsoft.checkt.testing.testValidation
import io.dwsoft.checkt.testing.violated
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.Gen
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.filterNot
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.of
import io.kotest.property.arbitrary.take
import io.kotest.property.exhaustive.map
import io.kotest.property.exhaustive.merge
import io.kotest.property.exhaustive.of

class ContainmentTests : FreeSpec({
    "${ContainsAny::class.simpleName}" - {
        forAll(containmentCases()) {
            "Check works" {
                when {
                    collection containsAnyOf expectedElements ->
                        collection shouldPass ContainsAny(expectedElements)
                    else -> collection shouldNotPass ContainsAny(expectedElements)
                }
            }

            "Rule works" {
                testValidation(
                    of = collection,
                    with = validation { +containsAnyOf(expectedElements) }
                ) {
                    when {
                        collection containsAnyOf expectedElements -> result.shouldBeValid()
                        else -> result.shouldBeInvalidBecause(
                            validated.violated<ContainsAny<*>> { msg ->
                                msg shouldContain "Value must contain any of $expectedElements"
                            }
                        )
                    }
                }
            }
        }
    }

    "${ContainsAll::class.simpleName}" - {
        forAll(containmentCases()) {
            "Check works" {
                when {
                    collection containsAllOf expectedElements ->
                        collection shouldPass ContainsAll(expectedElements)
                    else ->
                        collection shouldNotPass ContainsAll(expectedElements)
                }
            }

            "Rule works" {
                testValidation(
                    of = collection,
                    with = validation { +containsAllOf(expectedElements) }
                ) {
                    when {
                        collection containsAllOf expectedElements -> result.shouldBeValid()
                        else -> result.shouldBeInvalidBecause(
                            validated.violated<ContainsAll<*>> { msg ->
                                msg shouldContain "Value must contain all of $expectedElements"
                            }
                        )
                    }
                }
            }
        }
    }
})

private fun containmentCases(): Gen<ContainmentCase> {
    val casesForNotEmptyElementCollection = containmentCasesFor(anyCollection())
    val casesForEmptyElementCollection =
        casesForNotEmptyElementCollection
            .map { it.copy(expectedElements = emptyList()) }
    return casesForNotEmptyElementCollection.merge(casesForEmptyElementCollection)
}

private fun containmentCasesFor(baseCollection: Collection<Any>): Exhaustive<ContainmentCase> {
    return Exhaustive.of(
        emptyList(),
        collectionWithAllElementsFrom(baseCollection),
        collectionWithSomeElementsFrom(baseCollection),
        collectionWithoutElementsFrom(baseCollection),
        distinctProjectionOf(baseCollection),
    ).map {
        ContainmentCase(it, baseCollection)
    }
}

private fun collectionWithAllElementsFrom(collection: Collection<Any>): Collection<Any> =
    collection.toList()

private fun collectionWithSomeElementsFrom(collection: Collection<Any>): Collection<Any> =
    Arb.of(collection).take(minOf(1, collection.size / 2)).toList()

private fun collectionWithoutElementsFrom(collection: Collection<Any>): Collection<Any> =
    Arb.int().filterNot { it in collection }.take(collection.size).toList()

private fun distinctProjectionOf(collection: Collection<Any>): Collection<Any> =
    collection.toSet()

private fun anyCollection(): Collection<Any> {
    val duplicationRounds = Arb.int(5..15).next()
    return Arb.list(Arb.int(1..100), 5..10).next().toMutableList()
        .also { collection ->
            repeat(duplicationRounds) {
                collection += Arb.element(collection).next()
            }
        }
}

private infix fun <T> Collection<T>.containsAnyOf(other: Collection<T>): Boolean =
    other.any { it in this }

private infix fun <T> Collection<T>.containsAllOf(other: Collection<T>): Boolean =
    when {
        other.isEmpty() -> true
        this.isEmpty() -> false
        else -> {
            val mutableThis = this.toMutableList()
            other.all { mutableThis.remove(it) }
        }
    }

private data class ContainmentCase(
    val collection: Collection<Any>,
    val expectedElements: Collection<Any>,
)
