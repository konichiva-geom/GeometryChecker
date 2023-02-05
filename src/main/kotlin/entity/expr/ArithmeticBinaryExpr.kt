package entity.expr

import entity.expr.notation.Notation
import entity.expr.notation.NumNotation
import entity.expr.notation.PointNotation
import math.ArithmeticExpr
import math.FractionFactory
import pipeline.SymbolTable
import pipeline.interpreter.IdentMapper


private fun extractEntityNotation(expr: Expr): Notation? {
    if (expr is Notation)
        return if (expr is NumNotation) null else expr
    expr as BinaryExpr
    val left = extractEntityNotation(expr.left)
    if (left != null)
        return left
    val right = extractEntityNotation(expr.right)
    if (right != null)
        return right
    return null
}

class ParenthesesExpr(val expr: Expr) : Expr {
    override fun getChildren(): List<Expr> = listOf(expr)

    override fun getRepr(): StringBuilder = StringBuilder("($expr)")

    override fun mapIdents(mapper: IdentMapper): Expr {
        TODO("Not yet implemented")
    }

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun toString(): String = "($expr)"
}

class BinaryEquals(left: Expr, right: Expr) : BinaryExpr(left, right) {
    override fun getRepr() = getReprForBinaryWithExpressions(left, right, " == ")
    override fun mapIdents(mapper: IdentMapper) = BinaryEquals(left.mapIdents(mapper), right.mapIdents(mapper))

    override fun toString(): String {
        return "$left == $right"
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        left as ArithmeticExpr
        right as ArithmeticExpr
        return if (isEntityEquals(left, right))
            symbolTable.getRelationsByNotation(left.map.keys.first()) == symbolTable.getRelationsByNotation(right.map.keys.first())
        else false
    }

    override fun make(symbolTable: SymbolTable) {
        left as ArithmeticExpr
        right as ArithmeticExpr
        if (isEntityEquals(left, right)) {
            val leftNotation = left.map.keys.first()
            if (leftNotation is PointNotation)
                symbolTable.getPoint(leftNotation.p)
                    .mergePoints(leftNotation, right.map.keys.first() as PointNotation, symbolTable)
            else
                symbolTable.getRelationsByNotation(left.map.keys.first()).merge(right.map.keys.first(), symbolTable)
        } else {

        } //else throw SpoofError("Expected notations and arithmetic operations in equal relation")
    }
}

private fun isEntityEquals(left: ArithmeticExpr, right: ArithmeticExpr) =
    left.map.size == 1 && right.map.size == 1
            && left.map.values.first().contentEquals(FractionFactory.one())
            && right.map.values.first().contentEquals(FractionFactory.one())

class BinaryNotEquals(left: Expr, right: Expr) : BinaryExpr(left, right) {
    override fun getRepr() = getReprForBinaryWithExpressions(left, right, " != ")
    override fun mapIdents(mapper: IdentMapper) = BinaryNotEquals(left.mapIdents(mapper), right.mapIdents(mapper))
    override fun toString(): String {
        return "$left != $right"
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        left as ArithmeticExpr
        right as ArithmeticExpr
        if (isEntityEquals(left, right)) {
            val leftNotation = left.map.keys.first()
            val rightNotation = right.map.keys.first()
            if (leftNotation is PointNotation && rightNotation is PointNotation) {
                val leftPoint = symbolTable.getPoint(leftNotation)
                val rightPoint = symbolTable.getPoint(rightNotation)
                if (leftPoint == rightPoint)
                    return false
                if (leftPoint.unknown.contains(rightNotation.p) || rightPoint.unknown.contains(leftNotation.p))
                    return false
                return !(leftPoint.unknown.map { symbolTable.getPoint(it) }.toSet().contains(rightPoint)
                        || rightPoint.unknown.map { symbolTable.getPoint(it) }.toSet().contains(leftPoint))
            }
        }
        TODO("Not yet implemented")
    }

    override fun make(symbolTable: SymbolTable) {
        if (left is PointNotation && right is PointNotation) {
            val point = symbolTable.getPoint(left.p)
            point.merge(right, symbolTable) // FIXME it's wrong
            return
        }
        TODO("Not yet implemented")
    }
}

class BinaryGreater(left: Expr, right: Expr) : BinaryExpr(left, right) {
    override fun getRepr() = getReprForBinaryWithExpressions(left, right, " > ")
    override fun mapIdents(mapper: IdentMapper) = BinaryGreater(left.mapIdents(mapper), right.mapIdents(mapper))
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
    override fun mapIdents(mapper: IdentMapper) = BinaryGEQ(left.mapIdents(mapper), right.mapIdents(mapper))

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
