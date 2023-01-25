package expr

import PointCollection

/**
 * Points to equal point with the least lexicographical order
 *
 * Why does it matter? Say, there is a segment AB. We know some relations with this segment.
 * Then, we found out that A == C. And we access segment AC for the first time. It is actually same as AB, but we don't
 * know that. And there was no way to figure it out without PointPointer.
 */
class PointPointer {
    val points = mutableMapOf<String, String>()

    /**
     * map of renamed collections when key point becomes equal to a smaller point lexicographically
     */
    val subscribers = mutableMapOf<String, MutableSet<PointCollection>>()

    fun getIdentical(point:String) = points[point]!!
}