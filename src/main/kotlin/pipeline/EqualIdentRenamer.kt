package pipeline

import entity.Renamable
import entity.expr.notation.Notation
import entity.point_collection.PointCollection
import error.SpoofError
import utils.ExtensionUtils.addToOrCreateSet

/**
 * Maps to equal point (or circle) with the least lexicographical order
 *
 * Why does it matter? Say, there is a segment AB. We know some relations with this segment.
 * Then, we found out that A == C. And we access segment AC for the first time. It is actually same as AB, but we don't
 * know that. And there was no way to figure it out without IdentRenamer.
 */
class EqualIdentRenamer {
    private val points = mutableMapOf<String, String>()

    /**
     * map of renamed collections when key point becomes equal to a smaller point lexicographically
     */
    private val subscribers = mutableMapOf<String, MutableSet<Any>>()

    fun getIdentical(point: String) = points[point]!!

    fun addPoint(point: String) {
        points[point] = point
    }

    fun addSubscribers(pointCollection: PointCollection<*>, vararg points: String) {
        points.forEach { subscribers.addToOrCreateSet(it, pointCollection) }
    }

    fun addSubscribers(notation: Notation, vararg points: String) {
        points.forEach { subscribers.addToOrCreateSet(it, notation) }
    }

    fun removeSubscribers(collection: PointCollection<*>, vararg points: String) {
        points.forEach {
            if (subscribers[it] != null)
                subscribers[it]!!.remove(collection)
        }
    }

    /**
     * Works for points and circles
     */
    fun renameSubscribersAndPointer(prev: String, current: String, symbolTable: SymbolTable) {
        points[prev] = current
        if (subscribers[prev] != null) {
            subscribers[prev]!!.forEach {
                (it as Renamable).renameToMinimalAndRemap(symbolTable)
            }
            subscribers.addToOrCreateSet(current, *subscribers[prev]!!.toTypedArray())
            subscribers[prev]!!.clear()
            subscribers.remove(prev)
        }
    }

    /**
     * [n] shouldn't be bigger than 3. No need to create a common algorithm for all n
     */
    fun getAllNSizedPointLists(n: Int): List<List<String>> {
        val setOfPoints = points.values.toSet().filter { Regex("[A-Z]+\\w*").matches(it) }.toList()
        val m = setOfPoints.size
        val res = mutableListOf<List<String>>()
        when (n) {
            0 -> {
                res.add(mutableListOf())
            }
            1 -> {
                for (i in 0 until m)
                    res.add(mutableListOf(setOfPoints[i]))
            }
            2 -> {
                for (i in 0 until m)
                    for (j in 0 until m)
                        res.add(mutableListOf(setOfPoints[i], setOfPoints[j]))
            }
            3 -> {
                for (i in 0 until m)
                    for (j in 0 until m)
                        for (k in 0 until m)
                            res.add(mutableListOf(setOfPoints[i], setOfPoints[j], setOfPoints[k]))
            }
            else -> throw SpoofError("Do not use more than 3 enumeration variables: $n")
        }
        return res
    }

    fun clear() {
        points.clear()
        subscribers.clear()
    }
}
