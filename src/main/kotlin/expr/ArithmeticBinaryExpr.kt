package expr

import SymbolTable
import Utils.lambdaToSign
import Utils.mergeWithOperation
import pipeline.interpreter.ExpressionMapper

/**
 * Represents +, -, *, /
 */
class ArithmeticBinaryExpr(left: Expr, right: Expr, private val op: (Float, Float) -> Float) :
    BinaryExpr(left, right),
    Foldable {
    override fun flatten(): MutableMap<Any, Float> =
        (left as Foldable).flatten().mergeWithOperation((right as Foldable).flatten(), op)

    override fun getRepr() = getReprForBinaryWithExpressions(left, right, " ${lambdaToSign[op]} ")
    override fun rename(mapper: ExpressionMapper) =
        ArithmeticBinaryExpr(left.rename(mapper), right.rename(mapper), op)

    override fun toString(): String {
        return "$left${lambdaToSign[op]}$right"
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        TODO("Not yet implemented")
    }

    override fun make(symbolTable: SymbolTable) {
        TODO("Not yet implemented")
    }
}

class BinaryEquals(left: Expr, right: Expr) : BinaryExpr(left, right) {
    override fun getRepr() = getReprForBinaryWithExpressions(left, right, " == ")
    override fun rename(mapper: ExpressionMapper) = BinaryEquals(left.rename(mapper), right.rename(mapper))

    override fun toString(): String {
        return "$left == $right"
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        return if (left::class == right::class && left is Notation && right is Notation)
            symbolTable.getRelationsByNotation(left) == symbolTable.getRelationsByNotation(right)
        else false
    }

    override fun make(symbolTable: SymbolTable) {
        if (left is Notation && right is Notation)
            symbolTable.getRelationsByNotation(left).merge(right, symbolTable)
    }
}

class BinaryNotEquals(left: Expr, right: Expr) : BinaryExpr(left, right) {
    override fun getRepr() = getReprForBinaryWithExpressions(left, right, " != ")
    override fun rename(mapper: ExpressionMapper) = BinaryNotEquals(left.rename(mapper), right.rename(mapper))
    override fun toString(): String {
        return "$left != $right"
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        if (left is PointNotation && right is PointNotation) {
            val leftPoint = symbolTable.getPoint(left)
            val rightPoint = symbolTable.getPoint(right)
            if (leftPoint == rightPoint)
                return false
            if (leftPoint.unknown.contains(right.p) || rightPoint.unknown.contains(left.p))
                return false
            return !(leftPoint.unknown.map { symbolTable.getPoint(it) }.toSet().contains(rightPoint)
                || rightPoint.unknown.map { symbolTable.getPoint(it) }.toSet().contains(leftPoint))
        }
        TODO("Not yet implemented")
    }

    override fun make(symbolTable: SymbolTable) {
        if (left is PointNotation && right is PointNotation) {
            val point = symbolTable.getPoint(left.p)
            point.merge(right, symbolTable)
            return
        }
        TODO("Not yet implemented")
    }
}

class BinaryGreater(left: Expr, right: Expr) : BinaryExpr(left, right) {
    override fun getRepr() = getReprForBinaryWithExpressions(left, right, " > ")
    override fun rename(mapper: ExpressionMapper) = BinaryGreater(left.rename(mapper), right.rename(mapper))
    override fun toString(): String {
        return "$left > $right"
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        TODO("Not yet implemented")
    }

    override fun make(symbolTable: SymbolTable) {
        TODO("Not yet implemented")
    }
}

class BinaryGEQ(left: Expr, right: Expr) : BinaryExpr(left, right) {
    override fun getRepr() = getReprForBinaryWithExpressions(left, right, " >= ")
    override fun rename(mapper: ExpressionMapper) = BinaryGEQ(left.rename(mapper), right.rename(mapper))

    override fun toString(): String {
        return "$left >= $right"
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        TODO("Not yet implemented")
    }

    override fun make(symbolTable: SymbolTable) {
        TODO("Not yet implemented")
    }
}

/**
 * Sort left and right representations by:
 * 1. Notation
 * 2. Size of repr
 * 3. Hashcode, if nothing above works
 *
 * Not using hashcode for all to make it more predictable
 */
private fun getReprForBinaryWithExpressions(left: Expr, right: Expr, sign: String): StringBuilder {
    if (left is Notation && right is Notation)
        return if (left.getOrder() > right.getOrder())
            right.getRepr().append(sign).append(left.getRepr())
        else left.getRepr().append(sign).append(right.getRepr())
    val leftRepr = left.getRepr()
    val rightRepr = right.getRepr()
    if (leftRepr.length == rightRepr.length) {
        return if (leftRepr.hashCode() > rightRepr.hashCode())
            rightRepr.append(sign).append(leftRepr)
        else leftRepr.append(sign).append(rightRepr)
    }
    return if (leftRepr.length > rightRepr.length)
        right.getRepr().append(sign).append(left.getRepr())
    else left.getRepr().append(sign).append(right.getRepr())
}
