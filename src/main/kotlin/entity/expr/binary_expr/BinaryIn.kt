package entity.expr.binary_expr

import entity.expr.Relation
import entity.expr.notation.*
import entity.point_collection.PointCollection
import entity.point_collection.SegmentPointCollection
import pipeline.SymbolTable
import pipeline.interpreter.IdentMapper

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
        val pointList = if (left is PointNotation) listOf(left.p) else (left as Point2Notation).getPointsAndCircles()
        val collection = symbolTable.getKeyByNotation(right as Point2Notation) as PointCollection<*>
        collection.addPoints(pointList)
    }
}
