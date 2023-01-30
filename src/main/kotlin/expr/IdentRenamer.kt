package expr

import SymbolTable
import Utils.addToOrCreateSet
import pipeline.PointCollection

/**
 * Maps to equal point (or circle) with the least lexicographical order
 *
 * Why does it matter? Say, there is a segment AB. We know some relations with this segment.
 * Then, we found out that A == C. And we access segment AC for the first time. It is actually same as AB, but we don't
 * know that. And there was no way to figure it out without IdentRenamer.
 */
class IdentRenamer {
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

    /**
     * Works for points and circles
     */
    fun renameSubscribersAndPointer(prev: String, current: String, symbolTable: SymbolTable) {
        points[prev] = current
        if (subscribers[prev] != null) {
            subscribers[prev]!!.forEach {
                (it as Renamable).renameAndRemap(symbolTable)
            }
            subscribers.addToOrCreateSet(current, *subscribers[prev]!!.toTypedArray())
            subscribers[prev]!!.clear()
            subscribers.remove(prev)
        }
    }
}
