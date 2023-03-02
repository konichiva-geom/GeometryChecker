package utils

import error.SystemFatalError

/**
 * Base taken from: https://leetcode.com/problems/substring-with-concatenation-of-all-words/solutions/906972/kotlin-with-multiset-fast-and-simple/
 *
 */
class MultiSet<E>(val map: HashMap<E, Int>, override val size: Int) : MutableCollection<E> {
    constructor(array: Array<E>) : this(HashMap(), array.size) {
        array.forEach { add(it) }
    }

    override fun add(element: E): Boolean {
        map[element] = map.getOrDefault(element, 0) + 1
        return true
    }

    fun copy(): MultiSet<E> = MultiSet(HashMap(map), map.size)

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
        throw SystemFatalError("iterator not needed for multiset")
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

    companion object {
        fun <E : Any> multiSetOf(vararg elems: E): MultiSet<E> {
            return MultiSet(elems as Array<E>)
        }
    }
}