package pipeline

import expr.*

interface PointCollection<T : Notation> {
    fun getPointsInCollection(): Set<String>

    /**
     * Check that notation can be transformed into this collection
     */
    fun isFromNotation(notation: T): Boolean
    fun addPoints(added: List<String>)
}

data class LinePointCollection(val points: MutableSet<String>) : PointCollection<Point2Notation> {
    override fun getPointsInCollection(): Set<String> = points
    override fun isFromNotation(notation: Point2Notation) = points.containsAll(notation.getLetters())

    override fun addPoints(added: List<String>) {
        points.addAll(added)
    }
}

data class RayPointCollection(var start: String, val points: MutableSet<String>) : PointCollection<RayNotation> {
    override fun getPointsInCollection(): Set<String> = setOf(start) + points
    override fun isFromNotation(notation: RayNotation) = notation.p1 == start && points.contains(notation.p2)

    override fun addPoints(added: List<String>) {
        // TODO is it bad if added point is [start]?
        points.addAll(added)
    }
}

open class SegmentPointCollection(val bounds: MutableSet<String>, val points: MutableSet<String> = mutableSetOf()) :
    PointCollection<SegmentNotation> {
    override fun getPointsInCollection(): Set<String> = bounds + points
    override fun isFromNotation(notation: SegmentNotation) = bounds.containsAll(notation.getLetters())

    override fun addPoints(added: List<String>) {
        // TODO is it bad if added point is in [bounds]?
        points.addAll(added)
    }

    override fun toString(): String = "${bounds.joinToString(separator = "")}:$points"
}

class ArcPointCollection(
    val bounds: MutableSet<String>,
    val points: MutableSet<String> = mutableSetOf(),
    var circle: String
) :
    PointCollection<ArcNotation> {
    override fun getPointsInCollection() = bounds + points

    override fun addPoints(added: List<String>) {
        points.addAll(added)
    }

    override fun isFromNotation(notation: ArcNotation) = bounds.containsAll(notation.getLetters())
            && notation.circle == circle
}