package expr

import PointCollection
import Relation
import SegmentPointCollection
import SymbolTable
import SystemFatalError
import entity.LineRelations

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
    override fun check(symbolTable: SymbolTable): Boolean {
        symbolTable.getPointObjectsByNotation(right as Notation)
            .containsAll(symbolTable.getPointObjectsByNotation(left as Notation))
        throw SystemFatalError("Unexpected")
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
        val collection = symbolTable.getKeyByNotation(right as Point2Notation) as PointCollection
        collection.addPoints(pointList)
    }

    override fun toString(): String {
        return "$left in $right"
    }
}

class BinaryIntersects(left: Notation, right: Notation) : BinaryExpr(left, right), Returnable {
    lateinit var intersectionValue: Any // two circles intersect by array of points
    override fun getReturnValue(): Any = intersectionValue

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
                .map { symbolTable.getPoint(it) }.toSet().size == 1
        } else false
    }

    override fun make(symbolTable: SymbolTable) {
        val leftSet = symbolTable.getPointSetNotationByNotation(left as Notation)
        val rightSet = symbolTable.getPointSetNotationByNotation(right as Notation)
        val intersection = leftSet.intersect(rightSet)
        if (intersection.isNotEmpty()) {
            // TODO: maybe validate that all intersection points are the same object
            intersectionValue = PointNotation(intersection.first())
            return
        }
        val intersectionPoint = Utils.NameGenerator.getName()
    }
}

/**
 * `||` relation in tree
 */
class BinaryParallel(left: Point2Notation, right: Point2Notation) : BinaryExpr(left, right) {
    override fun toString(): String {
        return "$left || $right"
    }

    /**
     * Check all notations in [left] parallel set to see if one of them corresponds to the [right] [LineRelations]
     */
    override fun check(symbolTable: SymbolTable): Boolean {
        val line1 = (left as Point2Notation).toLine()
        val line2 = (right as Point2Notation).toLine()
        val lineRelations1 = symbolTable.getLine(line1)
        val lineRelations2 = symbolTable.getLine(line2)
        return lineRelations1.parallel.map { symbolTable.getLine(it) }.contains(lineRelations2)
            || lineRelations2.parallel.map { symbolTable.getLine(it) }.contains(lineRelations1)
    }

    override fun make(symbolTable: SymbolTable) {
        val line1 = (left as Point2Notation).toLine()
        val line2 = (right as Point2Notation).toLine()
        val lineRelations1 = symbolTable.getLine(line1)
        val lineRelations2 = symbolTable.getLine(line2)
        if (!lineRelations1.parallel.map { symbolTable.getLine(it) }.contains(lineRelations2))
            lineRelations1.parallel.add(line2)
        if (!lineRelations2.parallel.map { symbolTable.getLine(it) }.contains(lineRelations1))
            lineRelations1.parallel.add(line1)
    }
}

/**
 * `⊥` relation in tree
 */
class BinaryPerpendicular(left: Point2Notation, right: Point2Notation) : BinaryExpr(left, right) {
    override fun toString(): String {
        return "$left ⊥ $right"
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        val line1 = (left as Point2Notation).toLine()
        val line2 = (right as Point2Notation).toLine()
        val lineRelations1 = symbolTable.getLine(line1)
        val lineRelations2 = symbolTable.getLine(line2)
        return lineRelations1.perpendicular.map { symbolTable.getLine(it) }.contains(lineRelations2)
            || lineRelations2.perpendicular.map { symbolTable.getLine(it) }.contains(lineRelations1)
    }

    override fun make(symbolTable: SymbolTable) {
        val line1 = (left as Point2Notation).toLine()
        val line2 = (right as Point2Notation).toLine()
        val lineRelations1 = symbolTable.getLine(line1)
        val lineRelations2 = symbolTable.getLine(line2)
        if (!lineRelations1.perpendicular.map { symbolTable.getLine(it) }.contains(lineRelations2))
            lineRelations1.perpendicular.add(line2)
        if (!lineRelations2.perpendicular.map { symbolTable.getLine(it) }.contains(lineRelations1))
            lineRelations1.perpendicular.add(line1)
    }
}