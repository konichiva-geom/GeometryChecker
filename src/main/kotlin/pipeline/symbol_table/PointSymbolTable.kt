package pipeline.symbol_table

import entity.expr.binary_expr.BinaryNotEquals
import entity.expr.binary_expr.BinaryNotIn
import entity.expr.notation.IdentNotation
import entity.expr.notation.Point2Notation
import entity.expr.notation.PointNotation
import entity.expr.notation.TriangleNotation
import entity.relation.CircleRelations
import entity.relation.PointRelations
import entity.relation.TriangleRelations
import error.SpoofError
import math.ArithmeticExpr
import pipeline.interpreter.TheoremParser

open class PointSymbolTable : BaseSymbolTable() {
    protected val points = mutableMapOf<String, PointRelations>()
    protected val triangles = mutableMapOf<TriangleNotation, TriangleRelations>()

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

    fun resetCircle(newRelations: CircleRelations, notation: IdentNotation) {
        circles[notation]!!.unknown.forEach {
            circles[IdentNotation(it)]!!.unknown.remove(notation.text)
        }
        circles[notation] = newRelations
    }

    fun resetTriangle(newRelations: TriangleRelations, notation: TriangleNotation) {
        triangles[notation]!!.similarTriangles.forEach { it.similarTriangles.remove(it) }
        triangles[notation] = newRelations
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
     * check A !in line BC, C !in line BA, C !in line AB, A != B, A != C, B != C
     */
    fun addTriangle(triangleNotation: TriangleNotation, checkRelations: Boolean = false) {
        triangles[triangleNotation] = TriangleRelations()
        equalIdentRenamer.addSubscribers(triangleNotation, *triangleNotation.getPointsAndCircles().toTypedArray())

        if (!checkRelations)
            return

        val p1 = PointNotation(triangleNotation.p1)
        val p2 = PointNotation(triangleNotation.p2)
        val p3 = PointNotation(triangleNotation.p3)
        BinaryNotEquals(
            ArithmeticExpr(mutableMapOf(p1 to 1.0)),
            ArithmeticExpr(mutableMapOf(p2 to 1.0))
        ).check(this as SymbolTable)
        TheoremParser.check(
            BinaryNotEquals(
                ArithmeticExpr(mutableMapOf(p1 to 1.0)),
                ArithmeticExpr(mutableMapOf(p3 to 1.0))
            ), this
        )
        TheoremParser.check(
            BinaryNotEquals(
                ArithmeticExpr(mutableMapOf(p3 to 1.0)),
                ArithmeticExpr(mutableMapOf(p2 to 1.0))
            ), this
        )
        var hasAtLeastOne = false

        listOf(
            BinaryNotIn(p1, Point2Notation(p2.p, p3.p)),
            BinaryNotIn(p2, Point2Notation(p1.p, p3.p)),
            BinaryNotIn(p3, Point2Notation(p2.p, p1.p))
        ).forEach {
            if (it.check(this)) {
                hasAtLeastOne = true
                return@forEach
            }
        }
        if (!hasAtLeastOne)
            throw SpoofError("To create triangle at least one of the vertices should not be on line formed by other vertices")
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

    fun hasTriangle(triangleNotation: TriangleNotation): Boolean = triangles.containsKey(triangleNotation)


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
