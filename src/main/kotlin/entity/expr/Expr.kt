package entity.expr

import entity.expr.notation.IdentNotation
import entity.expr.notation.Notation
import entity.expr.notation.PointNotation
import error.SystemFatalError
import pipeline.interpreter.IdentMapper
import pipeline.interpreter.Signature
import pipeline.symbol_table.SymbolTable

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

class PointCreation(private val point: PointNotation, private val isDistinct: Boolean) : Expr, Creation {
    override fun getChildren(): List<Expr> {
        return listOf(point)
    }

    override fun getRepr(): StringBuilder = StringBuilder("${if (isDistinct) "distinct" else "new"} A")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapper) =
        PointCreation(PointNotation(mapper.get(point.p)), isDistinct)

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun create(symbolTable: SymbolTable) {
        if (isDistinct)
            symbolTable.distinctPoint(point.p)
        else
            symbolTable.newPoint(point.p)
    }

    override fun toString(): String {
        return "${if (isDistinct) "distinct" else "new"} ${point.p}"
    }
}

class CircleCreation(private val notation: IdentNotation, private val isDistinct: Boolean) : Expr, Creation {
    override fun getChildren(): List<Expr> {
        return listOf(notation)
    }

    override fun getRepr(): StringBuilder = StringBuilder(if (isDistinct) "distinct c" else "new c")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapper) =
        CircleCreation(IdentNotation(mapper.get(notation.text)), isDistinct)

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun create(symbolTable: SymbolTable) {
        if (isDistinct)
            symbolTable.distinctCircle(notation)
        else
            symbolTable.newCircle(notation)
    }

    override fun toString(): String {
        return if (isDistinct) "distinct $notation" else "new $notation"
    }
}
