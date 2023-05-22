package entity.expr.binary_expr

import entity.expr.Expr
import entity.expr.notation.IdentNotation
import entity.expr.notation.Point3Notation
import entity.expr.notation.PointNotation
import entity.expr.notation.SegmentNotation
import error.SpoofError
import external.WarnLogger
import math.ArithmeticExpr
import pipeline.interpreter.IdentMapperInterface
import pipeline.symbol_table.SymbolTable

class BinarySame(left: Expr, right: Expr) : BinaryExpr(left, right) {
    override fun getRepr() = getReprForBinaryWithExpressions(left, right, " === ")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface) = BinarySame(
        left.createNewWithMappedPointsAndCircles(mapper),
        right.createNewWithMappedPointsAndCircles(mapper)
    )

    override fun toString(): String = "$left == $right"

    override fun check(symbolTable: SymbolTable): Boolean {
        left as ArithmeticExpr
        right as ArithmeticExpr
        if (!isEntityEquals(left, right))
            throw SpoofError("=== operator can only be used for two notations, not arithmetic expressions")
        val leftNotation = left.map.keys.first()
        if (leftNotation is Point3Notation)
            throw SpoofError("Cannot use this operator for angles. To make points same, use == for points")
        if (leftNotation !is SegmentNotation)
            WarnLogger.warn("Use === only for segments, for other relations use ==")
        return symbolTable.getRelationsByNotation(leftNotation) ==
                symbolTable.getRelationsByNotation(right.map.keys.first())
    }

    override fun make(symbolTable: SymbolTable) {
        left as ArithmeticExpr
        right as ArithmeticExpr
        if (!isEntityEquals(left, right))
            throw SpoofError("=== operator can only be used for two notations, not arithmetic expressions")
        when (val leftNotation = left.map.keys.first()) {
            is PointNotation -> symbolTable.getPoint(leftNotation.p)
                .mergeOtherToThisPoint(leftNotation.p, (right.map.keys.first() as PointNotation).p, symbolTable)

            is IdentNotation -> symbolTable.getCircle(leftNotation)
                .mergeOtherToThisCircle(leftNotation, right.map.keys.first() as IdentNotation, symbolTable)

            else -> symbolTable.getRelationsByNotation(left.map.keys.first()).merge(right.map.keys.first(), symbolTable)
        }
    }
}
