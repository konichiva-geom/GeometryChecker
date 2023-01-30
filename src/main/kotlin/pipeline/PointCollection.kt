package pipeline

import SpoofError
import SymbolTable
import entity.EntityRelations
import expr.ArcNotation
import expr.IdentRenamer
import expr.Notation
import expr.Point2Notation
import expr.RayNotation
import expr.Renamable
import expr.SegmentNotation

interface PointCollection<T : Notation> : Renamable {
    fun getPointsInCollection(): Set<String>

    /**
     * Check that notation can be transformed into this collection
     */
    fun isFromNotation(notation: T): Boolean
    fun addPoints(added: List<String>)

    fun <T : EntityRelations?> getRelations(mapWithRelations: MutableMap<out PointCollection<*>, T>): T? {
        var relations: T? = null
        if (mapWithRelations[this] != null) {
            relations = mapWithRelations[this]
            mapWithRelations.remove(this)
        }

        return relations
    }
}

fun renamePointSet(set: MutableSet<String>, identRenamer: IdentRenamer) {
    val newPoints = set.map { identRenamer.getIdentical(it) }
    set.clear(); set.addAll(newPoints)
}

class LinePointCollection(val points: MutableSet<String>) : PointCollection<Point2Notation> {
    override fun getPointsInCollection(): Set<String> = points
    override fun isFromNotation(notation: Point2Notation) = points.containsAll(notation.getLetters())

    override fun addPoints(added: List<String>) {
        points.addAll(added)
    }

    override fun renameAndRemap(symbolTable: SymbolTable) {
        val lineRelations = getRelations(symbolTable.lines)

        renamePointSet(points, symbolTable.identRenamer)

        if (lineRelations != null)
            symbolTable.lines[this] = lineRelations
    }

    override fun checkValidityAfterRename() {
        if (points.size < 2)
            throw SpoofError("Cannot use notation with same points: %{point}%{point}", "point" to points.first())
    }

    override fun equals(other: Any?): Boolean {
        if (other !is LinePointCollection)
            return false
        return points.intersect(other.points).size > 2
    }

    override fun hashCode(): Int {
        return points.hashCode()
    }
}

class RayPointCollection(var start: String, val points: MutableSet<String>) : PointCollection<RayNotation> {
    override fun getPointsInCollection(): Set<String> = setOf(start) + points
    override fun isFromNotation(notation: RayNotation) = notation.p1 == start && points.contains(notation.p2)

    override fun addPoints(added: List<String>) {
        // TODO is it bad if added point is [start]?
        points.addAll(added)
    }

    override fun renameAndRemap(symbolTable: SymbolTable) {
        val rayRelations = getRelations(symbolTable.rays)

        renamePointSet(points, symbolTable.identRenamer)
        start = symbolTable.identRenamer.getIdentical(start)

        if (rayRelations != null)
            symbolTable.rays[this] = rayRelations
    }

    override fun checkValidityAfterRename() {
        if ((points - start).isEmpty())
            throw SpoofError("Cannot use notation with same points: %{point}%{point}", "point" to start)
    }

    /**
     * If only one point in points is same, rays are same
     */
    override fun equals(other: Any?): Boolean {
        if (other !is RayPointCollection)
            return false
        if (points.intersect(other.points).isEmpty())
            return false
        return start == other.start
    }

    /**
     * This won't work for finding collections in sets/maps.
     *
     * ```
     * val a = mutableSetOf(RayPointCollection("A", mutableSetOf("B", "C")))
     * a.contains(RayPointCollection("A", mutableSetOf("B"))) // false
     * ```
     */
    override fun hashCode(): Int {
        return points.hashCode() + 31 * start.hashCode()
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

    override fun renameAndRemap(symbolTable: SymbolTable) {
        val segmentRelations = getRelations(symbolTable.segments)

        renamePointSet(bounds, symbolTable.identRenamer)
        renamePointSet(points, symbolTable.identRenamer)

        if (segmentRelations != null)
            symbolTable.segments[this] = segmentRelations
    }

    override fun checkValidityAfterRename() {
        if (bounds.size < 2)
            throw SpoofError("Cannot use notation with same points: %{point}%{point}", "point" to points.first())
    }

    override fun toString(): String = "${bounds.joinToString(separator = "")}:$points"

    override fun equals(other: Any?): Boolean {
        if (other !is SegmentPointCollection)
            return false
        return bounds.intersect(other.bounds).size == bounds.size
    }

    override fun hashCode(): Int {
        return bounds.hashCode()
    }
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

    override fun renameAndRemap(symbolTable: SymbolTable) {
        val arcRelations = getRelations(symbolTable.arcs)

        renamePointSet(bounds, symbolTable.identRenamer)
        renamePointSet(points, symbolTable.identRenamer)
        circle = symbolTable.identRenamer.getIdentical(circle)

        if (arcRelations != null)
            symbolTable.arcs[this] = arcRelations
    }

    override fun checkValidityAfterRename() {
        if (points.size < 2)
            throw SpoofError("Cannot use notation with same points: %{point}%{point}", "point" to points.first())
    }

    override fun isFromNotation(notation: ArcNotation) = bounds.containsAll(notation.getLetters())
        && notation.circle == circle

    override fun equals(other: Any?): Boolean {
        if (other !is ArcPointCollection)
            return false
        if (bounds.intersect(other.bounds).size != bounds.size)
            return false
        return circle == other.circle
    }

    override fun hashCode(): Int {
        return bounds.hashCode() + 31 * circle.hashCode()
    }
}
