package entity.expr.binary_expr

import entity.expr.Expr
import entity.expr.notation.*
import entity.point_collection.PointCollection
import error.SpoofError
import error.SystemFatalError
import external.WarnLogger
import math.*
import pipeline.interpreter.IdentMapperInterface
import pipeline.symbol_table.SymbolTable
import utils.CommonUtils
import utils.ExtensionUtils.isAlmostZero
import utils.multiSetOf

class BinaryEquals(left: Expr, right: Expr) : BinaryExpr(left, right) {
    override fun getRepr() = getReprForBinaryWithExpressions(left, right, " == ")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface) = BinaryEquals(
        left.createNewWithMappedPointsAndCircles(mapper),
        right.createNewWithMappedPointsAndCircles(mapper)
    )

    override fun toString(): String {
        return "$left == $right"
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        left as ArithmeticExpr
        right as ArithmeticExpr
        return if (isNotArithmeticEntityEquals(left, right))
            symbolTable.getRelationsByNotation(left.map.keys.first()) ==
                    symbolTable.getRelationsByNotation(right.map.keys.first())
        else {
            val resolveLeft = vectorFromArithmeticMap(left.map, symbolTable)
            val resolveRight = vectorFromArithmeticMap(right.map, symbolTable)
            val isZeroVector = resolveLeft.mergeWithOperation(resolveRight, "-")
            return isZeroVector.all { it.value.isAlmostZero() }
        }
    }

    override fun make(symbolTable: SymbolTable) {
        left as ArithmeticExpr
        right as ArithmeticExpr

        if (isNotArithmeticEntityEquals(left, right)) {
            when (val leftNotation = left.map.keys.first()) {
                is PointNotation -> symbolTable.getPoint(leftNotation.p)
                    .mergeOtherToThisPoint(leftNotation.p, (right.map.keys.first() as PointNotation).p, symbolTable)

                is IdentNotation -> symbolTable.getCircle(leftNotation)
                    .mergeOtherToThisCircle(leftNotation, right.map.keys.first() as IdentNotation, symbolTable)

                else -> {
                    val (collectionLeft, relationsLeft) = symbolTable.getKeyValueByNotation(left.map.keys.first())
                    val (collectionRight, relationsRight) = symbolTable.getKeyValueByNotation(right.map.keys.first())
                    if (collectionLeft is PointCollection<*>) {
                        if (!CommonUtils.isSame(collectionLeft, collectionRight))
                            collectionLeft.merge(collectionRight as PointCollection<*>, symbolTable)
                        else return
                    } else {
                        // won't execute currently, but if something new is added will fail
                        if (collectionLeft !is IdentNotation)
                            throw SystemFatalError("Unexpected notation in equals")
                        throw SystemFatalError("Not done yet for circles")
                    }
                    relationsLeft.merge(null, symbolTable, relationsRight)
                }
            }
        } else {
            val (notation, result) = arithmeticExpressionsToVector(left, right, symbolTable)

            if (result.all { it.value.isAlmostZero() } || notation == null) {
                WarnLogger.warn("Expression %{expr} is already known", "expr" to this)
                return
            }
            if (result.keys.size == 1 && result.keys.first() == multiSetOf(0)) {
                throw SpoofError("Expression is incorrect")
            }

            when (notation) {
                is SegmentNotation -> symbolTable.segmentVectors.resolveVector(result as Vector)
                is Point3Notation -> symbolTable.angleVectors.resolveVector(result as Vector)
                is TriangleNotation -> {
                    checkTriangleEquation(left, right)
                    symbolTable.triangleVectors.resolveVector(result as Vector)
                }

                is MulNotation -> {
                    if (notation.elements.first() is SegmentNotation) {
                        symbolTable.segmentVectors.resolveVector(result as Vector)
                    } else if (notation.elements.first() is PointNotation) {
                        symbolTable.angleVectors.resolveVector(result as Vector)
                    } else throw SpoofError(
                        "Notation %{notation} is not supported in arithmetic expressions, " +
                                "use segments and angles",
                        "notation" to notation.elements.first()
                    )
                }

                else -> throw SpoofError(
                    "Notation %{notation} is not supported in arithmetic expressions, " +
                            "use segments and angles",
                    "notation" to notation
                )
            }
        }
    }
}
