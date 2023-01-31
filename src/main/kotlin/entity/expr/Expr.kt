package expr

import SymbolTable
import SystemFatalError
import entity.expr.Creation
import entity.expr.notation.IdentNotation
import entity.expr.notation.Notation
import entity.expr.notation.PointNotation
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
    fun mapIdents(mapper: IdentMapper): Expr
}

class AnyExpr(val notation: Notation) : Expr {
    override fun getChildren(): List<Expr> = listOf(notation)
    override fun getRepr(): StringBuilder = notation.getRepr().insert(0, "any ")
    override fun mapIdents(mapper: IdentMapper) = AnyExpr(notation.mapIdents(mapper) as Notation)

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }
}

/**
 * invocation
 * some_call(arg1, arg2) => A in CD
 * ^^^^^ signature ^^^^^ | ^output^
 */
class TheoremUse(val signature: Signature, val output: List<Expr>) : Expr {
    override fun getChildren(): List<Expr> = signature.args + output
    override fun getRepr(): StringBuilder = throw SystemFatalError("Unexpected getRepr() for TheoremUse")
    override fun mapIdents(mapper: IdentMapper): Expr {
        throw SystemFatalError("Unexpected rename() for TheoremUse. Remove this exception if theorems are called inside theorems")
        TheoremUse(
            Signature(signature.name, signature.args.map { it.mapIdents(mapper) }),
            output.map { it.mapIdents(mapper) }
        )
    }

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }
}

class PrefixNot(private val expr: Expr) : Expr {
    override fun getChildren(): List<Expr> = listOf(expr)
    override fun getRepr(): StringBuilder = expr.getRepr().insert(0, "not ")
    override fun mapIdents(mapper: IdentMapper) = PrefixNot(expr.mapIdents(mapper))

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
    override fun mapIdents(mapper: IdentMapper) = PointCreation(mapper.get(name))

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

    override fun getRepr(): StringBuilder = StringBuilder("new c")
    override fun mapIdents(mapper: IdentMapper) = PointCreation(mapper.get(name))

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun create(symbolTable: SymbolTable) {
        val res = IdentNotation(name)
        symbolTable.newCircle(res)
    }
}