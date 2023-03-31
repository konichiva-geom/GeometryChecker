package pipeline.symbol_table

import entity.expr.notation.IdentNotation
import entity.expr.notation.PointNotation
import entity.relation.CircleRelations
import entity.relation.PointRelations
import error.SpoofError

open class PointSymbolTable : BaseSymbolTable() {
    protected val points = mutableMapOf<String, PointRelations>()

    // IdentNotation is used for `rename and remap` function to work properly
    val circles = mutableMapOf<IdentNotation, CircleRelations>()

    /**
     * Wipe all existence of PointRelations and String:
     * 1. Set relations to relations of other point
     * 2. Delete notation from all [PointRelations.unknown]
     */
    fun resetPoint(newRelations: PointRelations, notation: String) {
        points[notation]!!.unknown.forEach {
            points[it]!!.unknown.remove(notation)
        }
        points[notation] = newRelations

    }

    /**
     * Make point add all others to unknown
     */
    fun newPoint(point: String): PointRelations {
        return addPoint(point) {
            val newPoint = PointRelations(points.keys.toMutableSet())
            for (p in points.keys)
                points[p]!!.unknown.add(point)
            newPoint
        }
    }

    fun distinctPoint(point: String): PointRelations {
        return addPoint(point) { PointRelations() }
    }

    private fun addPoint(point: String, specificActions: (String) -> PointRelations): PointRelations {
        if (points[point] != null)
            throw SpoofError("Point %{name} already defined", "name" to point)

        val pointRelations = specificActions(point)

        points[point] = pointRelations
        equalIdentRenamer.addPoint(point)
        return points[point]!!
    }

    /**
     * TODO: currently distinct and new circles are identical
     */
    fun distinctCircle(notation: IdentNotation): CircleRelations {
        return newCircle(notation)
    }

    fun newCircle(notation: IdentNotation): CircleRelations {
        if (circles[notation] != null)
            throw SpoofError("Circle %{name} already defined", "name" to notation.text)
        circles[notation] = CircleRelations()
        equalIdentRenamer.addPoint(notation.text)
        return circles[notation]!!
    }

    fun hasPoint(pointNotation: PointNotation): Boolean {
        return points[pointNotation.p] != null
    }

    fun hasPoint(point: String): Boolean {
        return points[point] != null
    }

    fun getPoint(name: String): PointRelations {
        return points[name] ?: throw SpoofError("Point %{name} is not instantiated", "name" to name)
    }

    fun getPoint(pointNotation: PointNotation): PointRelations {
        return getPoint(pointNotation.p)
    }

    fun getCircle(notation: IdentNotation): CircleRelations {
        var res = circles[notation]
        if (res == null) {
            res = CircleRelations()
            circles[notation] = res
        }
        return res
    }
}
