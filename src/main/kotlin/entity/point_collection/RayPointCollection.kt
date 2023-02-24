package entity.point_collection

import entity.Renamable
import entity.expr.notation.RayNotation
import entity.relation.AngleRelations
import entity.relation.RayRelations
import error.SpoofError
import math.Vector
import pipeline.SymbolTable

class RayPointCollection(private var start: String, private val points: MutableSet<String>) :
    PointCollection<RayNotation>() {
    private val angles = mutableSetOf<AnglePointCollection>()
    override fun getPointsInCollection(): Set<String> = setOf(start) + points
    override fun isFromNotation(notation: RayNotation) = notation.p1 == start && points.contains(notation.p2)

    override fun addPoints(added: List<String>, symbolTable: SymbolTable) {
        val relations = symbolTable.rays.remove(this)!!
        symbolTable.equalIdentRenamer.removeSubscribers(this, *added.toTypedArray())
        val anglePairs = mutableListOf<Pair<Vector?, AngleRelations?>>()
        for (angle in angles)
            anglePairs.add(angle.removeFromMaps(symbolTable))

        points.addAll(added)

        for ((i, angle) in angles.withIndex())
            angle.addToMaps(symbolTable, anglePairs[i].second, anglePairs[i].first)
        symbolTable.equalIdentRenamer.addSubscribers(this as Renamable, *added.toTypedArray())
        symbolTable.rays[this] = relations
    }

    override fun renameToMinimalAndRemap(symbolTable: SymbolTable) {
        var rayRelations: RayRelations? = null
        for (rayPointCollection in symbolTable.rays.keys) {
            if (rayPointCollection == this) {
                rayRelations = symbolTable.rays[rayPointCollection]
                symbolTable.rays.remove(rayPointCollection)
                break
            }
        }

        renamePointSet(points, symbolTable.equalIdentRenamer)
        start = symbolTable.equalIdentRenamer.getIdentical(start)

        for (rayCollection in symbolTable.rays.keys) {
            if (this == rayCollection) {
                val oldRelation = symbolTable.rays.remove(rayCollection)!!
                this.merge(rayCollection, symbolTable)
                if (rayRelations != null)
                    oldRelation.merge(null, symbolTable, rayRelations)
                symbolTable.rays[this] = oldRelation
                return
            }
        }
        symbolTable.rays[this] = rayRelations!!
    }

    override fun checkValidityAfterRename(): Exception? {
        if ((points - start).isEmpty())
            return SpoofError("Cannot use notation with same points: %{point}%{point}", "point" to start)
        return null
    }

    /**
     * If only one point in points is same, rays are same
     */
    override fun equals(other: Any?): Boolean {
        if (other !is RayPointCollection)
            return false
        if ((points - start).intersect(other.points - other.start).isEmpty())
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

    override fun toString(): String = "$start:$points"
    override fun merge(other: PointCollection<*>, symbolTable: SymbolTable) {
        other as RayPointCollection
        assert(start == other.start)
        angles.addAll(other.angles)
        symbolTable.equalIdentRenamer.removeSubscribers(this, *other.points.toTypedArray())

        val anglePairs = mutableListOf<Pair<Vector?, AngleRelations?>>()
        for (angle in angles)
            anglePairs.add(angle.removeFromMaps(symbolTable))

        points.addAll(other.points)

        symbolTable.equalIdentRenamer.addSubscribers(this as Renamable, *other.points.toTypedArray())
        for ((i, angle) in angles.withIndex())
            angle.addToMaps(symbolTable, anglePairs[i].second, anglePairs[i].first)
    }

    fun addAngle(angle: AnglePointCollection) {
        angles.add(angle)
    }
}
