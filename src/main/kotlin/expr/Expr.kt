package expr

import Signature
import SymbolTable

/**
 * Expression that returns some value, e.g. [BinaryIntersects] returns point, or segment, or something else
 */
interface Returnable {
    fun getReturnValue(): Any
}

interface Foldable {
    fun flatten(): MutableMap<Any, Float> = mutableMapOf(this to 1f)
}

/**
 * Interface for creation (points, circles)
 */
interface Creation {
    fun create(symbolTable: SymbolTable)
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

class PrefixNot(private val expr: Expr) : Expr {
    override fun getChildren(): List<Expr> = listOf(expr)
    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "not $expr"
    }
}

class PointCreation(private val name: String) : Expr, Creation {
    override fun getChildren(): List<Expr> {
        return emptyList()
    }

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun create(symbolTable: SymbolTable) {
        val res = PointNotation(name)
        symbolTable.newPoint(res)
    }
}

class CircleCreation(private val name: String) : Expr, Creation {
    override fun getChildren(): List<Expr> {
        return emptyList()
    }

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun create(symbolTable: SymbolTable) {
        val res = IdentNotation(name)
        symbolTable.newCircle(res)
    }
}