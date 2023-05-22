package entity.expr.binary_expr

import entity.expr.Expr
import entity.expr.notation.*
import error.SpoofError
import math.ArithmeticExpr
import math.Vector
import math.mergeWithOperation
import math.vectorFromArithmeticMap
import pipeline.symbol_table.SymbolTable

internal fun isEntityEquals(left: ArithmeticExpr, right: ArithmeticExpr) =
    left.map.size == 1 && right.map.size == 1
            && left.map.values.first() == 1.0
            && right.map.values.first() == 1.0

internal fun isNotArithmeticEntityEquals(left: ArithmeticExpr, right: ArithmeticExpr) =
    isEntityEquals(left, right)
            && left.map.keys.first() !is Point3Notation
            && left.map.keys.first() !is SegmentNotation
            && left.map.keys.first() !is TriangleNotation

internal fun arithmeticExpressionsToVector(
    left: ArithmeticExpr,
    right: ArithmeticExpr,
    symbolTable: SymbolTable
): Pair<Notation?, Vector> {
    val resolveLeft = vectorFromArithmeticMap(left.map, symbolTable)
    val resolveRight = vectorFromArithmeticMap(right.map, symbolTable)
    val notation = left.map.mergeWithOperation(right.map, "-")
        .keys.firstOrNull { it !is NumNotation }

    val result = resolveLeft.mergeWithOperation(resolveRight, "-")
    return notation to result
}

internal fun checkTriangleEquation(left: ArithmeticExpr, right: ArithmeticExpr) {
    if (left.map.size != 1 || right.map.size != 1
        || left.map.keys.first() !is TriangleNotation
        || right.map.keys.first() !is TriangleNotation
    ) {
        throw SpoofError("Triangle equations shouldn't contain any expressions besides triangles")
    }
}

internal fun incorrectNotation(notation: Notation, isAngleCorrect: Boolean = true): Nothing {
    throw SpoofError(
        "Notation %{notation} is not supported in arithmetic expressions, " +
                "use segments, angles${if (isAngleCorrect) " and angles" else ""}",
        "notation" to notation
    )
}

/**
 * Sort left and right representations by:
 * 1. Notation
 * 2. Size of repr
 * 3. Hashcode, if nothing above works
 *
 * Not using hashcode for all to make it more predictable
 */
internal fun getReprForBinaryWithExpressions(left: Expr, right: Expr, sign: String): StringBuilder {
    if (left is Notation && right is Notation)
        return if (left.getOrder() > right.getOrder())
            right.getRepr().append(sign).append(left.getRepr())
        else left.getRepr().append(sign).append(right.getRepr())
    val leftRepr = left.getRepr()
    val rightRepr = right.getRepr()
    if (leftRepr.length == rightRepr.length) {
        return if (leftRepr.toString().hashCode() > rightRepr.toString().hashCode())
            rightRepr.append(sign).append(leftRepr)
        else leftRepr.append(sign).append(rightRepr)
    }
    return if (leftRepr.length > rightRepr.length)
        right.getRepr().append(sign).append(left.getRepr())
    else left.getRepr().append(sign).append(right.getRepr())
}
