package entity.expr

import entity.expr.notation.Notation
import entity.expr.notation.NumNotation
import entity.expr.notation.PointNotation
import error.SpoofError
import math.Vector
import math.fromNotation
import math.mergeWith
import pipeline.SymbolTable
import pipeline.interpreter.IdentMapper
import kotlin.reflect.KClass

/**
 * Represents +, -, *, /
 */
class ArithmeticBinaryExpr(left: Expr, right: Expr, private val op: String) :
    BinaryExpr(left, right) {

    override fun getRepr() = getReprForBinaryWithExpressions(left, right, " $op ")
    override fun mapIdents(mapper: IdentMapper) =
        ArithmeticBinaryExpr(left.mapIdents(mapper), right.mapIdents(mapper), op)

    override fun toString(): String {
        return "$left $op $right"
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        TODO("Not yet implemented")
    }

    override fun make(symbolTable: SymbolTable) {
        createVectors(symbolTable)
    }

    fun createVectors(symbolTable: SymbolTable): Vector {
        val leftVector = createVector(left, symbolTable)
        val rightVector = createVector(right, symbolTable)
        return leftVector.mergeWith(rightVector, op)
    }

    fun getType(): KClass<out Expr> {
        if (left is Notation)
            return left::class
        return (left as ArithmeticBinaryExpr).getType()
    }
}

private fun createVector(expr: Expr, symbolTable: SymbolTable): Vector {
    return if (expr is ArithmeticBinaryExpr)
        expr.createVectors(symbolTable)
    else fromNotation(symbolTable, expr as Notation)
}

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
        return if (left::class == right::class && left is Notation && right is Notation)
            symbolTable.getRelationsByNotation(left) == symbolTable.getRelationsByNotation(right)
        else false
    }

    override fun make(symbolTable: SymbolTable) {
        if (left is Notation && right is Notation) {
            if (left is PointNotation)
                symbolTable.getPoint(left.p).mergePoints(left, right as PointNotation, symbolTable)
            else
                symbolTable.getRelationsByNotation(left).merge(right, symbolTable)
        } else if (left is ArithmeticBinaryExpr || right is ArithmeticBinaryExpr) {
            // TODO convert arithmetic expr to vector by walking a tree recursively
            val leftVector = createVector(left, symbolTable)
            val rightVector = createVector(right, symbolTable)
            val notation = extractEntityNotation(this)
        } else throw SpoofError("Expected notations and arithmetic operations in equal relation")
    }
}

class BinaryNotEquals(left: Expr, right: Expr) : BinaryExpr(left, right) {
    override fun getRepr() = getReprForBinaryWithExpressions(left, right, " != ")
    override fun mapIdents(mapper: IdentMapper) = BinaryNotEquals(left.mapIdents(mapper), right.mapIdents(mapper))
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
