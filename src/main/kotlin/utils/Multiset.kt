package utils

import error.SystemFatalError
import utils.ExtensionUtils.addOrCreate

fun <E : Any> multiSetOf(vararg elems: E): MultiSet<E> {
    return MultiSet(elems as Array<E>)
}

/**
 * Base taken from: https://leetcode.com/problems/substring-with-concatenation-of-all-words/solutions/906972/kotlin-with-multiset-fast-and-simple/
 * TODO: make immutable
 */
class MutableMultiSet<E : Any>(val map: HashMap<E, Int>, override val size: Int) : MutableCollection<E> {
    constructor(array: Array<E>) : this(HashMap(), array.size) {
        array.forEach { add(it) }
    }

    override fun add(element: E): Boolean {
        map[element] = map.getOrDefault(element, 0) + 1
        return true
    }

    fun toMultiSet(): MultiSet<E> = MultiSet(HashMap(map.toMap()), size)
    fun copy(): MutableMultiSet<E> = MutableMultiSet(HashMap(map), map.size)

    override fun isEmpty() = map.isEmpty()
    override fun remove(element: E): Boolean =
        map[element]?.let { if (it == 1) map.remove(element) else map[element] = it - 1; true } ?: false

    override fun contains(element: E): Boolean {
        return map.contains(element)
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        return map.keys.containsAll(elements)
    }

    override fun addAll(elements: Collection<E>): Boolean {
        for (e in elements)
            add(e)
        return elements.isNotEmpty()
    }

    override fun clear() {
        map.clear()
    }

    override fun iterator(): MutableIterator<E> {
        return map.keys.toMutableSet().iterator()
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        var res = false
        for (e in elements)
            if (map.remove(e) != null)
                res = true
        return res
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        throw SystemFatalError("retainAll not needed for multiset")
    }

    override fun hashCode(): Int {
        return map.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is MutableMultiSet<*>)
            return false
        return map == other.map
    }

    override fun toString(): String {
        return map.toString()
    }
}

class MultiSet<E : Any>(val map: Map<E, Int>, override val size: Int) : Collection<E> {
    constructor(array: Array<E>) : this(initMultiSet(array), array.size)

    fun copy(): MultiSet<E> = MultiSet(map.toMap(), size)
    fun toMutableMultiSet(): MutableMultiSet<E> = MutableMultiSet(HashMap(map.toMap()), size)


    override fun contains(element: E): Boolean {
        return map.contains(element)
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        return map.keys.containsAll(elements)
    }

    override fun isEmpty(): Boolean {
        return map.isEmpty()
    }

    override fun iterator(): Iterator<E> {
        return map.keys.iterator()
    }

    override fun hashCode(): Int {
        return map.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is MultiSet<*>)
            return false
        return map == other.map
    }

    override fun toString(): String {
        return "Multiset:${map}"
    }
}

private fun <E> initMultiSet(array: Array<E>): Map<E, Int> {
    val res = mutableMapOf<E, Int>()
    array.forEach { res.addOrCreate(it, 1) }
    return res.toMap()
}