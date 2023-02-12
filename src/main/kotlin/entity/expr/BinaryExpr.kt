package entity.expr

import com.github.h0tk3y.betterParse.utils.Tuple4
import entity.expr.notation.*
import entity.point_collection.PointCollection
import entity.point_collection.SegmentPointCollection
import entity.relation.CircleRelations
import entity.relation.LineRelations
import entity.relation.Relation
import error.SpoofError
import external.Logger
import pipeline.SymbolTable
import pipeline.interpreter.IdentMapper
import utils.NameGenerator

abstract class BinaryExpr(val left: Expr, val right: Expr) : Expr, Relation {
    override fun getChildren(): List<Expr> = listOf(left, right)

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }
}

/**
 * `in` relation in tree
 */
class BinaryIn(left: Notation, right: Notation) : BinaryExpr(left, right), Relation {
    override fun getRepr(): StringBuilder = left.getRepr().append(" in ").append(right.getRepr())
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapper) =
        BinaryIn(
            left.createNewWithMappedPointsAndCircles(mapper) as Notation,
            right.createNewWithMappedPointsAndCircles(mapper) as Notation
        )

    override fun toString(): String {
        return "$left in $right"
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        return symbolTable.getPointObjectsByNotation(right as Notation)
            .containsAll(symbolTable.getPointObjectsByNotation(left as Notation))
    }

    override fun make(symbolTable: SymbolTable) {
        if (right is IdentNotation)
            symbolTable.getCircle(right).points.add((left as PointNotation).p)
        if (right is ArcNotation) {
            val arcCollection = symbolTable.getKeyByNotation(right as Notation) as SegmentPointCollection
            val collection = symbolTable.getKeyByNotation(left as Notation)
            // left is a point
            if (collection is String) arcCollection.points.add(collection)
            // left is an arc
            else arcCollection.points.addAll((collection as SegmentPointCollection).getPointsInCollection())
        }
        val pointList = if (left is PointNotation) listOf(left.p) else (left as Point2Notation).getLetters()
        val collection = symbolTable.getKeyByNotation(right as Point2Notation) as PointCollection<*>
        collection.addPoints(pointList)
    }
}

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
        val intersectionValue = if (intersection.isNotEmpty())
            PointNotation(intersection.first())
        else PointNotation(NameGenerator.getName())
        if (intersection.isEmpty()) {
            symbolTable.newPoint(intersectionValue)
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
        Logger.warn("This relation is already made")
    }

    private fun addPointsToCircleOrLinear(
        symbolTable: SymbolTable,
        notation: Notation,
        intersectionValue: List<String>
    ) {
        val (collection, relations) = symbolTable.getKeyValueByNotation(notation)
        if (collection is PointCollection<*>)
            collection.addPoints(intersectionValue)
        else (relations as CircleRelations).points.addAll(intersectionValue)
    }
}

/**
 * `||` relation in tree
 */
class BinaryParallel(left: Point2Notation, right: Point2Notation) : BinaryExpr(left, right) {
    override fun getRepr(): StringBuilder = left.getRepr().append(" parallel ").append(right.getRepr())
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapper) =
        BinaryParallel(
            left.createNewWithMappedPointsAndCircles(mapper) as Point2Notation,
            right.createNewWithMappedPointsAndCircles(mapper) as Point2Notation
        )

    override fun toString(): String {
        return "$left || $right"
    }

    /**
     * Check all notations in [left] parallel set to see if one of them corresponds to the [right] [LineRelations]
     */
    override fun check(symbolTable: SymbolTable): Boolean {
        val (_, lineRelations1, _, lineRelations2) = getLinesAndLineRelations(left, right, symbolTable)
        return lineRelations1.parallel.map { symbolTable.getLine(it) }.contains(lineRelations2)
                || lineRelations2.parallel.map { symbolTable.getLine(it) }.contains(lineRelations1)
    }

    override fun make(symbolTable: SymbolTable) {
        val (line1, lineRelations1, line2, lineRelations2) = getLinesAndLineRelations(left, right, symbolTable)
        if (!lineRelations1.parallel.map { symbolTable.getLine(it) }.contains(lineRelations2))
            lineRelations1.parallel.add(line2)
        if (!lineRelations2.parallel.map { symbolTable.getLine(it) }.contains(lineRelations1))
            lineRelations2.parallel.add(line1)
    }
}

/**
 * `⊥` relation in tree
 */
class BinaryPerpendicular(left: Point2Notation, right: Point2Notation) : BinaryExpr(left, right) {
    override fun getRepr(): StringBuilder = left.getRepr().append(" perpendicular ").append(right.getRepr())
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapper) =
        BinaryPerpendicular(
            left.createNewWithMappedPointsAndCircles(mapper) as Point2Notation,
            right.createNewWithMappedPointsAndCircles(mapper) as Point2Notation
        )

    override fun toString(): String {
        return "$left ⊥ $right"
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        val (_, lineRelations1, _, lineRelations2) = getLinesAndLineRelations(left, right, symbolTable)
        return lineRelations1.perpendicular.map { symbolTable.getLine(it) }.contains(lineRelations2)
                || lineRelations2.perpendicular.map { symbolTable.getLine(it) }.contains(lineRelations1)
    }

    override fun make(symbolTable: SymbolTable) {
        val (line1, lineRelations1, line2, lineRelations2) = getLinesAndLineRelations(left, right, symbolTable)
        if (!lineRelations1.perpendicular.map { symbolTable.getLine(it) }.contains(lineRelations2))
            lineRelations1.perpendicular.add(line2)
        if (!lineRelations2.perpendicular.map { symbolTable.getLine(it) }.contains(lineRelations1))
            lineRelations2.perpendicular.add(line1)
    }
}

class BinaryAssignment(left: Notation, right: Expr) : BinaryExpr(left, right) {
    override fun getChildren(): List<Expr> {
        return listOf(right)
    }

    override fun getRepr(): StringBuilder = left.getRepr().append(" = ").append(right.getRepr())

    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapper): Expr =
        BinaryAssignment(
            left.createNewWithMappedPointsAndCircles(mapper) as Notation,
            right.createNewWithMappedPointsAndCircles(mapper)
        )

    override fun check(symbolTable: SymbolTable): Boolean {
        if (!(right as BinaryExpr).check(symbolTable))
            return false
        if (right !is Returnable)
            throw SpoofError("Cannot assign from %{expr}", "expr" to right)
        if (left is PointNotation) {
            if (!symbolTable.hasPoint(left))
                return false
            return symbolTable.getPoint(left) == symbolTable.getPoint(right.getReturnValue(symbolTable) as String)
        } else throw SpoofError("Assigning non-points is not yet implemented")
    }

    override fun make(symbolTable: SymbolTable) {
        if (!(right as BinaryExpr).check(symbolTable))
            throw SpoofError("prove %{expr} first, then assign", "expr" to right)
        if (right !is Returnable)
            throw SpoofError("Cannot assign from %{expr}", "expr" to right)
        if (left is PointNotation) {
            val newRelations = symbolTable.newPoint(left)
            val assignValue = (right as Returnable).getReturnValue(symbolTable)
            if (assignValue.size != 1)
                throw SpoofError("Expected 1 assign point")
            newRelations.mergeOtherToThisPoint(left, PointNotation(assignValue.first()), symbolTable)
        } else throw SpoofError("Assigning non-points is not yet implemented")
    }
}

private fun getLinesAndLineRelations(
    first: Expr,
    second: Expr,
    symbolTable: SymbolTable
): Tuple4<Point2Notation, LineRelations, Point2Notation, LineRelations> {
    val line1 = (first as Point2Notation).toLine()
    val line2 = (second as Point2Notation).toLine()
    return Tuple4(
        line1, symbolTable.getLine(line1),
        line2, symbolTable.getLine(line2)
    )
}
