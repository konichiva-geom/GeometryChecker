package pipeline.symbol_table

import entity.expr.notation.IdentNotation
import entity.expr.notation.PointNotation
import entity.relation.CircleRelations
import entity.relation.PointRelations
import error.SpoofError

open class PointSymbolTable : BaseSymbolTable() {
    protected val points = mutableMapOf<String, PointRelations>()

    val circles = mutableMapOf<IdentNotation, CircleRelations>() // IdentNotation is used to rename and remap to work

    fun resetPoint(newRelations: PointRelations, notation: String) {
        points[notation] = newRelations
    }

    /**
     * Make point distinct from all others
     */
    fun newPoint(point: String): PointRelations {
        if (points[point] != null)
            throw SpoofError("Point %{name} already defined", "name" to point)
        points[point] = PointRelations()
        equalIdentRenamer.addPoint(point)
        return points[point]!!
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