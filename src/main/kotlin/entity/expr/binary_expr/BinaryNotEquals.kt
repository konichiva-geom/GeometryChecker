package entity.expr.binary_expr

import entity.expr.Expr
import entity.expr.notation.Point3Notation
import entity.expr.notation.PointNotation
import entity.expr.notation.SegmentNotation
import entity.expr.notation.TriangleNotation
import error.SpoofError
import math.ArithmeticExpr
import math.MulNotation
import math.minus
import pipeline.interpreter.IdentMapperInterface
import pipeline.symbol_table.SymbolTable
import utils.ExtensionUtils.isAlmostZero
import utils.multiSetOf

class BinaryNotEquals(left: Expr, right: Expr) : BinaryExpr(left, right) {
    override fun getRepr() = getReprForBinaryWithExpressions(left, right, " != ")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface) = BinaryNotEquals(
        left.createNewWithMappedPointsAndCircles(mapper),
        right.createNewWithMappedPointsAndCircles(mapper)
    )

    override fun toString(): String {
        return "$left != $right"
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        left as ArithmeticExpr
        right as ArithmeticExpr

        if (isNotArithmeticEntityEquals(left, right)) {
            val leftNotation = left.map.keys.first()
            val rightNotation = right.map.keys.first()

            if (leftNotation is PointNotation && rightNotation is PointNotation) {
                val leftPoint = symbolTable.getPoint(leftNotation)
                val rightPoint = symbolTable.getPoint(rightNotation)

                if (leftPoint === rightPoint
                    || leftPoint.unknown.contains(rightNotation.p)
                    || rightPoint.unknown.contains(leftNotation.p)
                )
                    return false
                return !(leftPoint.unknown.map { symbolTable.getPoint(it) }.toSet().contains(rightPoint)
                        || rightPoint.unknown.map { symbolTable.getPoint(it) }.toSet().contains(leftPoint))
            } else {
                TODO("Not yet implemented")
            }
        }
        val (notation, vector) = arithmeticExpressionsToVector(left, right, symbolTable)
        val negated = vector.minus()
        if (vector.all { it.value.isAlmostZero() }) {
            throw SpoofError("Expression is incorrect")
        }
        if (notation == null)
            return true
        return when (notation) {
            is SegmentNotation -> symbolTable.segmentVectors.notEqualVectors.contains(vector)
                    || symbolTable.segmentVectors.notEqualVectors.contains(negated)

            is Point3Notation -> symbolTable.angleVectors.notEqualVectors.contains(vector)
                    || symbolTable.angleVectors.notEqualVectors.contains(negated)

            is TriangleNotation -> {
                checkTriangleEquation(left, right)
                (symbolTable.triangleVectors.notEqualVectors.contains(vector)
                        || symbolTable.triangleVectors.notEqualVectors.contains(negated))
            }

            else -> incorrectNotation(notation)
        }
    }

    override fun make(symbolTable: SymbolTable) {
        left as ArithmeticExpr
        right as ArithmeticExpr

        if (isNotArithmeticEntityEquals(left, right)) {
            val leftNotation = left.map.keys.first()
            val rightNotation = right.map.keys.first()
            if (leftNotation is PointNotation && rightNotation is PointNotation) {
                symbolTable.getPoint(leftNotation.p).unknown.remove(rightNotation.p)
                symbolTable.getPoint(rightNotation.p).unknown.remove(leftNotation.p)
                return
            }
        } else {
            val (notation, vector) = arithmeticExpressionsToVector(left, right, symbolTable)

            if (vector.all { it.value.isAlmostZero() }) {
                throw SpoofError("Expression is incorrect")
            }

            if (notation == null || (vector.keys.size == 1 && vector.keys.first() == multiSetOf(0)))
                return

            when (notation) {
                is SegmentNotation -> symbolTable.segmentVectors.addNotEqualVector(vector)
                is Point3Notation -> symbolTable.angleVectors.addNotEqualVector(vector)
                is TriangleNotation -> {
                    checkTriangleEquation(left, right)
                    symbolTable.triangleVectors.addNotEqualVector(vector)
                }

                is MulNotation -> {
                    TODO("Not yet implemented")
                }

                else -> incorrectNotation(notation)
            }
        }
    }
}