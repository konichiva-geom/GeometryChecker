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

class BinaryGreater(left: Expr, right: Expr) : BinaryExpr(left, right) {
    override fun getRepr() = getReprForBinaryWithExpressions(left, right, " > ")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface) = BinaryGreater(
        left.createNewWithMappedPointsAndCircles(mapper),
        right.createNewWithMappedPointsAndCircles(mapper)
    )

    override fun toString(): String {
        return "$left > $right"
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        left as ArithmeticExpr
        right as ArithmeticExpr

        val (notation, vector) = arithmeticExpressionsToVector(left, right, symbolTable)

        if (vector.all { it.value.isAlmostZero() }
            || (vector.keys.size == 1 && vector.keys.first() == multiSetOf(0) && vector.values.first() <= 0)) {
            throw SpoofError("Expression is incorrect")
        }
        // vector is majorly bigger than zero
        if (vector.all { it.value >= 0 })
            return true
        if (notation == null)
            return true
        return when (notation) {
            is SegmentNotation -> symbolTable.segmentVectors.isBiggerContained(vector)
            is Point3Notation -> symbolTable.angleVectors.isBiggerContained(vector)
            is MulNotation -> TODO("Not yet implemented")
            else -> incorrectNotation(notation, false)
        }
    }

    override fun make(symbolTable: SymbolTable) {
        left as ArithmeticExpr
        right as ArithmeticExpr

        val (notation, vector) = arithmeticExpressionsToVector(left, right, symbolTable)

        if (vector.all { it.value.isAlmostZero() }
            || (vector.keys.size == 1 && vector.keys.first() == multiSetOf(0) && vector.values.first() <= 0)) {
            throw SpoofError("Expression is incorrect")
        }
        if (notation == null)
            return
        when (notation) {
            is SegmentNotation -> symbolTable.segmentVectors.addBiggerVector(vector)
            is Point3Notation -> symbolTable.angleVectors.addBiggerVector(vector)
            is MulNotation -> TODO("Not yet implemented")
            else -> incorrectNotation(notation, false)
        }
    }
}