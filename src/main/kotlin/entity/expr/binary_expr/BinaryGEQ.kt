package entity.expr.binary_expr

import entity.expr.Expr
import entity.expr.notation.Point3Notation
import entity.expr.notation.SegmentNotation
import error.SpoofError
import math.ArithmeticExpr
import math.MulNotation
import pipeline.interpreter.IdentMapperInterface
import pipeline.symbol_table.SymbolTable
import utils.ExtensionUtils.isAlmostZero
import utils.multiSetOf

class BinaryGEQ(left: Expr, right: Expr) : BinaryExpr(left, right) {
    override fun getRepr() = getReprForBinaryWithExpressions(left, right, " >= ")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface) =
        BinaryGEQ(left.createNewWithMappedPointsAndCircles(mapper), right.createNewWithMappedPointsAndCircles(mapper))

    override fun toString(): String {
        return "$left >= $right"
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        left as ArithmeticExpr
        right as ArithmeticExpr

        val (notation, vector) = arithmeticExpressionsToVector(left, right, symbolTable)

        if (vector.keys.size == 1 && vector.keys.first() == multiSetOf(0) && vector.values.first() <= 0) {
            throw SpoofError("Expression is incorrect")
        }
        // vector is majorly bigger than zero
        if (notation == null || vector.all { it.value.isAlmostZero() || it.value >= 0.0 })
            return true
        return when (notation) {
            is SegmentNotation -> symbolTable.segmentVectors.isBiggerOrEqualContained(vector)
            is Point3Notation -> symbolTable.angleVectors.isBiggerOrEqualContained(vector)
            is MulNotation -> TODO("Not yet implemented")
            else -> incorrectNotation(notation, false)
        }
    }

    override fun make(symbolTable: SymbolTable) {
        left as ArithmeticExpr
        right as ArithmeticExpr

        val (notation, vector) = arithmeticExpressionsToVector(left, right, symbolTable)

        if (vector.keys.size == 1 && vector.keys.first() == multiSetOf(0) && vector.values.first() < 0) {
            throw SpoofError("Expression is incorrect")
        }
        if (notation == null || vector.all { it.value.isAlmostZero() || it.value >= 0.0 })
            return
        when (notation) {
            is SegmentNotation -> symbolTable.segmentVectors.addBiggerOrEqualVector(vector)
            is Point3Notation -> symbolTable.angleVectors.addBiggerOrEqualVector(vector)
            is MulNotation -> TODO("Not yet implemented")
            else -> incorrectNotation(notation, false)
        }
    }
}