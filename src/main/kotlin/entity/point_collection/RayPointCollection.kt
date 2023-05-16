package entity.point_collection

import entity.Renamable
import entity.expr.notation.RayNotation
import error.SpoofError
import math.Vector
import math.changeAllPairs
import pipeline.symbol_table.SymbolTable
import utils.multiSetOf

class RayPointCollection(var start: String, private val points: MutableSet<String>) :
    PointCollection<RayNotation>() {
    val angles = mutableSetOf<AnglePointCollection>()
    override fun getPointsInCollection(): Set<String> = setOf(start) + points
    override fun isFromNotation(notation: RayNotation) = notation.p1 == start && points.contains(notation.p2)

    override fun addPoints(added: List<String>, symbolTable: SymbolTable) {
        val filtered = added.filter { it != start }
        withRemappingAnglesAndSubscribers(symbolTable) {
            points.addAll(filtered)
        }
    }

    override fun renameToMinimalAndRemap(symbolTable: SymbolTable) {
        withRemappingAnglesAndSubscribers(symbolTable) {
            renamePointSet(points, symbolTable.equalIdentRenamer)
            start = symbolTable.equalIdentRenamer.getIdentical(start)
        }
    }

    private fun withRemappingAnglesAndSubscribers(symbolTable: SymbolTable, block: (SymbolTable) -> Unit) {
        symbolTable.equalIdentRenamer.removeSubscribers(this as Renamable, *points.toTypedArray())

        val angleVectors = mutableListOf<Vector?>()
        for (angle in angles)
            angleVectors.add(removeValueFromMap(symbolTable.angleVectors.vectors, angle))

        block(symbolTable)

        for ((i, angle) in angles.withIndex()) {
            val changeInVectorIndices = addToMap(
                angleVectors[i],
                symbolTable.angleVectors,
                angle,
                symbolTable
            )
            if (changeInVectorIndices != null) {
                for (j in (i + 1) until angles.size) {
                    if (angleVectors[j] != null && angleVectors[j]!![multiSetOf(changeInVectorIndices.first)] != null) {
                        angleVectors[j]!!.changeAllPairs(changeInVectorIndices)
                    }
                }
            }
        }

        symbolTable.equalIdentRenamer.addSubscribers(this as Renamable, *points.toTypedArray())
        mergeEntitiesInList(symbolTable.rays, symbolTable)
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
        addPoints(other.points.toList(), symbolTable)
    }

    fun addAngle(angle: AnglePointCollection) {
        angles.add(angle)
    }

    fun removeUnexistingAngles(symbolTable: SymbolTable) {
        val iter = angles.iterator()
        val tableAngles = symbolTable.angles.map { it.e1 }
        while (iter.hasNext()) {
            val angle = iter.next()
            if (tableAngles.find { it === angle } == null)
                iter.remove()
        }

        val anglesWithoutRepetitions = angles.toMutableSet()
        angles.clear()
        angles.addAll(anglesWithoutRepetitions)
    }
}
