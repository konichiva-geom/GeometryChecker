package entity.point_collection

import entity.expr.notation.RayNotation
import error.SpoofError
import pipeline.SymbolTable

class RayPointCollection(var start: String, val points: MutableSet<String>) : PointCollection<RayNotation> {
    override fun getPointsInCollection(): Set<String> = setOf(start) + points
    override fun isFromNotation(notation: RayNotation) = notation.p1 == start && points.contains(notation.p2)

    override fun addPoints(added: List<String>) {
        points.addAll(added)
    }

    override fun renameToMinimalAndRemap(symbolTable: SymbolTable) {
        val rayRelations = getRelations(symbolTable.rays)

        renamePointSet(points, symbolTable.equalIdentRenamer)
        start = symbolTable.equalIdentRenamer.getIdentical(start)

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
