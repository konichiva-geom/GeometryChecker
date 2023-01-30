package expr

import SymbolTable
import SystemFatalError
import pipeline.interpreter.ExpressionMapper
import pipeline.interpreter.Signature

/**
 * Expression that returns some value, e.g. [BinaryIntersects] returns point, or segment, or something else
 */
interface Returnable {
    fun getReturnValue(): Any
}
/**
 * Interface for creation (points, circles)
 */
interface Creation {
    fun create(symbolTable: SymbolTable)
}

interface Expr : Comparable<Expr> {
    fun getChildren(): List<Expr>

    fun getRepr(): StringBuilder

    /**
     * Create a new instance where all idents are mapped with [mapper]
     *
     * Used for theorem and inference interpreting
     */
    fun mapIdents(mapper: ExpressionMapper): Expr
}

class AnyExpr(val notation: Notation) : Expr {
    override fun getChildren(): List<Expr> = listOf(notation)
    override fun getRepr(): StringBuilder = notation.getRepr().insert(0, "any ")
    override fun mapIdents(mapper: ExpressionMapper) = AnyExpr(notation.mapIdents(mapper) as Notation)

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
    override fun mapIdents(mapper: ExpressionMapper): Expr {
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
    override fun mapIdents(mapper: ExpressionMapper) = PrefixNot(expr.mapIdents(mapper))

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
    override fun mapIdents(mapper: ExpressionMapper) = PointCreation(mapper.get(name))

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
    override fun mapIdents(mapper: ExpressionMapper) = PointCreation(mapper.get(name))

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun create(symbolTable: SymbolTable) {
        val res = IdentNotation(name)
        symbolTable.newCircle(res)
    }
}