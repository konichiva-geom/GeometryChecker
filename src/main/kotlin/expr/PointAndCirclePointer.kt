package expr

import PointCollection
import Utils.addToOrCreateSet

/**
 * Maps to equal point (or circle) with the least lexicographical order
 *
 * Why does it matter? Say, there is a segment AB. We know some relations with this segment.
 * Then, we found out that A == C. And we access segment AC for the first time. It is actually same as AB, but we don't
 * know that. And there was no way to figure it out without PointPointer.
 */
class PointAndCirclePointer {
    private val points = mutableMapOf<String, String>()

    /**
     * map of renamed collections when key point becomes equal to a smaller point lexicographically
     */
    private val subscribers = mutableMapOf<String, MutableSet<PointCollection>>()

    fun getIdentical(point: String) = points[point]!!

    fun addSubscribers(pointCollection: PointCollection, vararg points: String) {
        points.forEach { subscribers.addToOrCreateSet(it, pointCollection) }
    }
}