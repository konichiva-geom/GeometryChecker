package entity.expr

import entity.expr.notation.IdentNotation
import entity.expr.notation.Notation
import entity.expr.notation.PointNotation
import error.SystemFatalError
import pipeline.symbol_table.SymbolTable
import pipeline.interpreter.IdentMapper
import pipeline.interpreter.Signature

interface Expr : Comparable<Expr> {
    fun getChildren(): List<Expr>

    fun getRepr(): StringBuilder

    /**
     * Create a new instance where all idents are mapped with [mapper]
     *
     * Used for theorem and pipeline.inference interpreting
     */
    fun createNewWithMappedPointsAndCircles(mapper: IdentMapper): Expr

    fun traverseExpr(symbolTable: SymbolTable, lambda: (expr: Expr, SymbolTable) -> Boolean): Boolean {
        if (lambda(this, symbolTable))
            return true
        getChildren().forEach {
            if (it.traverseExpr(symbolTable, lambda))
                return true
        }
        return false
    }
}

class AnyExpr(val notation: Notation) : Expr {
    override fun getChildren(): List<Expr> = listOf(notation)
    override fun getRepr(): StringBuilder = notation.getRepr().insert(0, "any ")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapper) =
        AnyExpr(notation.createNewWithMappedPointsAndCircles(mapper) as Notation)

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "any $notation"
    }
}

/**
 * some_call(arg1, arg2) => A in CD
 *
 * ^^^^ signature ^^^^ | ^output^
 */
class Invocation(val signature: Signature, val output: List<Expr>) : Expr {
    override fun getChildren(): List<Expr> = signature.args + output
    override fun getRepr(): StringBuilder = throw SystemFatalError("Unexpected getRepr() for TheoremUse")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapper): Expr {
        throw SystemFatalError("Unexpected rename() for TheoremUse. Remove this exception if theorems are called inside theorems")
        Invocation(
            Signature(signature.name, signature.args.map { it.createNewWithMappedPointsAndCircles(mapper) }),
            output.map { it.createNewWithMappedPointsAndCircles(mapper) }
        )
    }

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }
}

class PrefixNot(private val expr: Expr) : Expr {
    override fun getChildren(): List<Expr> = listOf(expr)
    override fun getRepr(): StringBuilder = expr.getRepr().insert(0, "not ")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapper) =
        PrefixNot(expr.createNewWithMappedPointsAndCircles(mapper))

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

    override fun getRepr(): StringBuilder = StringBuilder("new A")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapper) = PointCreation(mapper.get(name))

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun create(symbolTable: SymbolTable) {
        val res = PointNotation(name)
        symbolTable.newPoint(res.p)
    }

    override fun toString(): String {
        return "new $name"
    }
}

class CircleCreation(private val name: String) : Expr, Creation {
    override fun getChildren(): List<Expr> {
        return emptyList()
    }

    override fun getRepr(): StringBuilder = StringBuilder("new c")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapper) = PointCreation(mapper.get(name))

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun create(symbolTable: SymbolTable) {
        val res = IdentNotation(name)
        symbolTable.newCircle(res)
    }

    override fun toString(): String {
        return "new $name"
    }
}