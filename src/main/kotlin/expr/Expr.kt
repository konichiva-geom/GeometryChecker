package expr

import Relation
import Signature
import SymbolTable
import Utils.lambdaToSign
import Utils.mergeWithOperation

interface Foldable {
    fun flatten(): MutableMap<Any, Float> = mutableMapOf(this to 1f)
}

interface Expr : Comparable<Expr> {
    fun run(symbolTable: SymbolTable) {
    }

    fun getChildren(): List<Expr>
}

class TheoremUse(val signature: Signature, val output: List<Expr>) : Expr {
    override fun getChildren(): List<Expr> = signature.args + output

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }
}

class MockExpr : Expr {
    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun getChildren(): List<Expr> {
        TODO("Not yet implemented")
    }

    override fun toString(): String = "%"
}

abstract class BinaryExpr(val left: Expr, val right: Expr) : Expr, Relation {
    override fun getChildren(): List<Expr> = listOf(left, right)

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }
}

class PrefixNot(private val expr: Expr) : Expr {
    override fun getChildren(): List<Expr> = listOf(expr)
    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "not $expr"
    }
}

class BinaryIn(left: Notation, right: Notation) : BinaryExpr(left, right), Relation {
    override fun check(symbolTable: SymbolTable): Boolean {
        // segment AB in segment AB
        if (left == right)
            return true
        if (left is PointNotation && right is Point2Notation) {
            // A in ray AB
            if ((right.p1 == left.p || right.p2 == left.p))
                return true
            // A == C; C in AB
            val pointEntity = symbolTable.getPoint(left)
            if (symbolTable.getPoint(right.p1) == pointEntity || symbolTable.getPoint(right.p2) == pointEntity)
                return true
        }
        return symbolTable.getRelationsByNotation(left as Notation).isIn(right as Notation)
    }

    override fun make(symbolTable: SymbolTable) {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "$left in $right"
    }
}

class BinaryIntersects(left: Notation, right: Notation) : BinaryExpr(left, right) {
    override fun toString(): String {
        return "$left ∩ $right"
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        return if (left is Notation && right is Notation) {
            symbolTable.getPointSetNotationByNotation(left)
                .intersect(symbolTable.getPointSetNotationByNotation(right))
                .isNotEmpty() && left.getOrder() < right.getOrder()
        } else false
    }

    override fun make(symbolTable: SymbolTable) {
        //symbolTable.
    }
}

class BinaryParallel(left: Notation, right: Notation) : BinaryExpr(left, right) {
    override fun toString(): String {
        return "$left || $right"
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        TODO("Not yet implemented")
    }

    override fun make(symbolTable: SymbolTable) {
    }
}

class BinaryPerpendicular(left: Notation, right: Notation) : BinaryExpr(left, right) {
    override fun toString(): String {
        return "$left ⊥ $right"
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        TODO("Not yet implemented")
    }

    override fun make(symbolTable: SymbolTable) {
        TODO("Not yet implemented")
    }
}

class BinaryEquals(left: Expr, right: Expr) : BinaryExpr(left, right) {
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
    override fun toString(): String {
        return "$left != $right"
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        TODO("Not yet implemented")
    }

    override fun make(symbolTable: SymbolTable) {
        TODO("Not yet implemented")
    }
}

class BinaryGreater(left: Expr, right: Expr) : BinaryExpr(left, right) {
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

class ArithmeticBinaryExpr(left: Expr, right: Expr, private val op: (Float, Float) -> Float) : BinaryExpr(left, right),
    Foldable {
    override fun flatten(): MutableMap<Any, Float> =
        (left as Foldable).flatten().mergeWithOperation((right as Foldable).flatten(), op)

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