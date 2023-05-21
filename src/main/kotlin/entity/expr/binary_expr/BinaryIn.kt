package entity.expr.binary_expr

import entity.expr.Expr
import entity.expr.Relation
import entity.expr.notation.*
import entity.point_collection.PointCollection
import error.SpoofError
import pipeline.symbol_table.SymbolTable
import pipeline.interpreter.IdentMapperInterface

/**
 * `in` relation in tree
 */
class BinaryIn(left: Notation, right: Notation) : BinaryExpr(left, right), Relation {
    override fun getRepr(): StringBuilder = left.getRepr().append(" in ").append(right.getRepr())
    override fun toString(): String = "$left in $right"
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface) =
        BinaryIn(
            left.createNewWithMappedPointsAndCircles(mapper) as Notation,
            right.createNewWithMappedPointsAndCircles(mapper) as Notation
        )

    override fun check(symbolTable: SymbolTable): Boolean {
        return symbolTable.getPointObjectsByNotation(right as Notation)
            .containsAll(symbolTable.getPointObjectsByNotation(left as Notation))
    }

    override fun make(symbolTable: SymbolTable) {
        if (right is IdentNotation) {
            symbolTable.getCircle(right).addPoints(symbolTable, (left as PointNotation).p)
            return
        } else if (right is ArcNotation) {
            TODO("not yet implemented")
//            val arcCollection = symbolTable.getKeyByNotation(right as Notation) as SegmentPointCollection
//            val collection = symbolTable.getKeyByNotation(left as Notation)
//            // left is a point
//            if (collection is String) arcCollection.points.add(collection)
//            // left is an arc
//            else arcCollection.points.addAll((collection as SegmentPointCollection).getPointsInCollection())
        } else if (right is TriangleNotation) {
            symbolTable.getTriangle(right).pointsInside.add((left as PointNotation).p)
            return
        }
        val pointList = if (left is PointNotation) listOf(left.p) else (left as Point2Notation).getPointsAndCircles()
        val collection = symbolTable.getKeyByNotation(right as Point2Notation) as PointCollection<*>
        collection.addPoints(pointList, symbolTable)
    }
}

class BinaryNotIn(left: Notation, right: Notation) : BinaryExpr(left, right), Relation {
    override fun getRepr(): StringBuilder = left.getRepr().append(" !in ").append(right.getRepr())
    override fun toString(): String = "$left !in $right"

    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface): Expr = BinaryNotIn(
        left.createNewWithMappedPointsAndCircles(mapper) as Notation,
        right.createNewWithMappedPointsAndCircles(mapper) as Notation
    )

    override fun check(symbolTable: SymbolTable): Boolean {
        checkNotContainable(symbolTable)
        return (left as Notation).getPointsAndCircles()
            .all {
                (symbolTable.getRelationsByNotation(right as Notation) as NotContainable)
                    .pointsNotContained.contains(it)
            }
    }

    override fun make(symbolTable: SymbolTable) {
        checkNotContainable(symbolTable)
        return (left as Notation).getPointsAndCircles()
            .forEach {
                (symbolTable.getRelationsByNotation(right as Notation) as NotContainable)
                    .pointsNotContained.add(it)
            }
    }

    private fun checkNotContainable(symbolTable: SymbolTable) {
        if (symbolTable.getRelationsByNotation(right as Notation) !is NotContainable)
            throw SpoofError("Expected NotContainable as right operand, got %{right}", "right" to right)
    }
}

/**
 * Interface for `!in` relation
 */
interface NotContainable {
    val pointsNotContained: MutableSet<String>

    fun mergeDifferentPoints(other: NotContainable) {
        this.pointsNotContained.addAll(other.pointsNotContained)
        other.pointsNotContained.clear()
    }
}
