package entity.expr.binary_expr

import entity.expr.Returnable
import entity.expr.notation.Notation
import entity.expr.notation.Point2Notation
import entity.expr.notation.PointNotation
import entity.point_collection.PointCollection
import entity.relation.CircleRelations
import error.SpoofError
import external.WarnLogger
import pipeline.SymbolTable
import pipeline.interpreter.IdentMapper
import utils.NameGenerator

/**
 * Intersection means that two line objects are not on the same line
 * In this case:
 * segment AB in CD
 * segment BE in CD
 *
 * segments AB and BE are not intersecting, but have a common point
 */
class BinaryIntersects(left: Notation, right: Notation) : BinaryExpr(left, right), Returnable {
    /**
     * two circles intersect by array of points
     */
    override fun getReturnValue(symbolTable: SymbolTable): Set<String> {
        return symbolTable.getPointSetNotationByNotation(left as Notation)
            .intersect(symbolTable.getPointSetNotationByNotation(right as Notation))
            .map { symbolTable.equalIdentRenamer.getIdentical(it) }
            .toSet()
    }

    override fun getRepr(): StringBuilder = left.getRepr().append(" intersects ").append(right.getRepr())
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapper) =
        BinaryIntersects(
            left.createNewWithMappedPointsAndCircles(mapper) as Notation,
            right.createNewWithMappedPointsAndCircles(mapper) as Notation
        )

    override fun toString(): String {
        return "$left ∩ $right"
    }

    /**
     * Check that exactly one point is in intersection
     */
    override fun check(symbolTable: SymbolTable): Boolean {
        return if (left is Notation && right is Notation) {
            symbolTable.getPointSetNotationByNotation(left)
                .intersect(symbolTable.getPointSetNotationByNotation(right))
                .map { symbolTable.getPoint(it) }.toSet().isNotEmpty()
        } else false
    }

    override fun make(symbolTable: SymbolTable) {
        val intersection = getReturnValue(symbolTable)
        val intersectionValue = PointNotation(
            if (intersection.isNotEmpty())
                intersection.first()
            else if (left is Point2Notation && right is Point2Notation)
                symbolTable.nameGenerator.getName()
            else symbolTable.nameGenerator.getUnknownPointQuantityName()
        )
        if (intersection.isEmpty()) {
            symbolTable.newPoint(intersectionValue.p)
            addPointsToCircleOrLinear(symbolTable, left as Notation, listOf(intersectionValue.p))
            addPointsToCircleOrLinear(symbolTable, right as Notation, listOf(intersectionValue.p))
            return
        }
        if (intersection.map { symbolTable.getPoint(it) }.toSet().size > 1)
            throw SpoofError(
                "This task is incorrect. There can be only one intersection point between two lines, " +
                        "but got a second one from: %{expr}",
                "expr" to this
            )
        WarnLogger.warn("This relation is already made")
    }

    fun addPointsToCircleOrLinear(
        symbolTable: SymbolTable,
        notation: Notation,
        intersectionValue: List<String>
    ) {
        val (collection, relations) = symbolTable.getKeyValueByNotation(notation)
        if (collection is PointCollection<*>)
            collection.addPoints(intersectionValue, symbolTable)
        else
            (relations as CircleRelations).addPoints(symbolTable, *intersectionValue.toTypedArray())
    }
}
