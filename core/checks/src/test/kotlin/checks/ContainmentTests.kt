package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.testing.testsFor
import io.kotest.core.spec.style.FreeSpec
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
    testsFor(containmentCases()) {
        fromCase(take = { collection }) {
            check { ContainsAny(case.other) } shouldPassWhen { value containsAnyOf case.other }

            rule { containsAnyOf(case.other) } shouldPassWhen { value containsAnyOf case.other } orFail
                    { withMessage("Collection must contain any of elements specified") }

            check { ContainsAll(case.other) } shouldPassWhen { value containsAllOf case.other }

            rule { containsAllOf(case.other) } shouldPassWhen { value containsAllOf case.other } orFail
                    { withMessage("Collection must contain all of elements specified") }

            check { ContainsNone(case.other) } shouldPassWhen { value containsNoneOf case.other }

            rule { containsNoneOf(case.other) } shouldPassWhen { value containsNoneOf case.other } orFail
                    { withMessage("Collection must not contain any of elements specified") }
        }
    }
})

private fun containmentCases(): Gen<ContainmentCase> {
    val casesForNotEmptyElementCollection = containmentCasesFor(anyCollection())
    val casesForEmptyElementCollection =
        casesForNotEmptyElementCollection
            .map { it.copy(other = emptyList()) }
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

private infix fun <T> Collection<T>.containsNoneOf(other: Collection<T>): Boolean =
    other.none { it in this }

private data class ContainmentCase(
    val collection: Collection<Any>,
    val other: Collection<Any>,
)
