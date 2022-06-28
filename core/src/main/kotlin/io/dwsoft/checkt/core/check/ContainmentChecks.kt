package io.dwsoft.checkt.core.check

import io.dwsoft.checkt.core.check.Check.Context

class ContainsAny<V>(vararg elements: V) :
    Check<Collection<V>, ContainsAny.Key, ContainsAny.Params<V>>
{
    private val elements: Set<V> = elements.toSet()
    override val context: Context<Key, Params<V>> = Context.of(Key, Params(this.elements))

    override fun invoke(value: Collection<V>): Boolean = value.any { elements.contains(it) }

    object Key : Check.Key
    data class Params<V>(val elements: Collection<V>) : Check.Params()
}

class ContainsAll<V>(vararg elements: V) :
    Check<Collection<V>, ContainsAll.Key, ContainsAll.Params<V>>
{
    private val elements: List<V> = elements.toList()
    override val context: Context<Key, Params<V>> = Context.of(Key, Params(this.elements))
    private val elementsCountMap: Map<V, Int> = this.elements.toCountMap()

    override fun invoke(value: Collection<V>): Boolean = value.containsAllElements()

    private fun Collection<V>.containsAllElements(): Boolean =
        this.toCountMap().let { validatedCountMap ->
            elementsCountMap.all { (element, minimalCount) ->
                (validatedCountMap[element] ?: 0) >= minimalCount
            }
        }

    private fun Collection<V>.toCountMap(): Map<V, Int> {
        return this.groupingBy { it }.eachCount()
    }

    object Key : Check.Key
    data class Params<V>(val elements: Collection<V>) : Check.Params()
}
