package entity.expr

import entity.expr.binary_expr.BinaryNotEquals
import entity.expr.binary_expr.BinaryNotIn
import entity.expr.notation.*
import error.SpoofError
import error.SystemFatalError
import math.ArithmeticExpr
import pipeline.inference.InferenceProcessor
import pipeline.interpreter.IdentMapperInterface
import pipeline.interpreter.Signature
import pipeline.interpreter.TheoremParser
import pipeline.symbol_table.SymbolTable

/**
 * Общий Интерфейс для выражений из AST
 */
interface Expr : Comparable<Expr> {
    fun getChildren(): List<Expr>

    fun getAllChildren(): List<Expr> {
        val res = mutableSetOf<Expr>()
        val children = this.getChildren()
        res.addAll(children)
        for (child in children) {
            res.addAll(child.getAllChildren())
        }
        return res.toList()
    }

    fun getRepr(): StringBuilder

    /**
     * Create a new instance where all idents are mapped with [mapper]
     *
     * Used for theorem and pipeline.inference interpreting
     */
    fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface): Expr
}

fun Expr.traverseExpr(symbolTable: SymbolTable, lambda: (expr: Expr, SymbolTable) -> Boolean): Boolean {
    if (lambda(this, symbolTable))
        return true
    getChildren().forEach {
        if (it.traverseExpr(symbolTable, lambda))
            return true
    }
    return false
}

class AnyExpr(val notation: Notation) : Expr {
    override fun getChildren(): List<Expr> = listOf(notation)
    override fun getRepr(): StringBuilder = notation.getRepr().insert(0, "any ")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface) =
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
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface): Expr {
        throw SystemFatalError("Unexpected rename() for TheoremUse. Remove this exception if theorems are called inside theorems")
        Invocation(
            Signature(signature.name, signature.args.map { it.createNewWithMappedPointsAndCircles(mapper) }),
            output.map { it.createNewWithMappedPointsAndCircles(mapper) }
        )
    }

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun toString(): String = signature.toString()
}

class PrefixNot(private val expr: Expr) : Expr {
    override fun getChildren(): List<Expr> = listOf(expr)
    override fun getRepr(): StringBuilder = expr.getRepr().insert(0, "not ")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface) =
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
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface) =
        PointCreation(PointNotation(mapper.get(point.p)), isDistinct)

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun create(symbolTable: SymbolTable, inferenceProcessor: InferenceProcessor) {
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
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface) =
        CircleCreation(IdentNotation(mapper.get(notation.text)), isDistinct)

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun create(symbolTable: SymbolTable, inferenceProcessor: InferenceProcessor) {
        if (isDistinct)
            symbolTable.distinctCircle(notation)
        else
            symbolTable.newCircle(notation)
    }

    override fun toString(): String {
        return if (isDistinct) "distinct $notation" else "new $notation"
    }
}

class TriangleCreation(private val notation: TriangleNotation) : Expr, Creation {
    override fun getChildren(): List<Expr> {
        return mutableListOf(notation)
    }

    override fun getRepr(): StringBuilder = StringBuilder("new $notation")

    override fun toString(): String = "new $notation"

    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface): Expr {
        return TriangleCreation(notation.createNewWithMappedPointsAndCircles(mapper) as TriangleNotation)
    }

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    /**
     * new ABC => A !in line BC, A != B, A != C, B != C
     */
    override fun create(symbolTable: SymbolTable, inferenceProcessor: InferenceProcessor) {
        if (symbolTable.hasTriangle(notation))
            throw SpoofError("Triangle %{triangle} already exists", "triangle" to notation)

        // in this case do not need to create relations in the comment
        if (checkTriangles(symbolTable)) {
            symbolTable.addTriangle(notation, checkRelations = false)
            return
        }

        val alreadyCreatedPoints = mutableListOf<String>()
        val newPoints = mutableListOf<String>()
        notation.getPointsAndCircles().forEach {
            if (symbolTable.hasPoint(it))
                alreadyCreatedPoints.add(it)
            else {
                PointCreation(PointNotation(it), isDistinct = false).create(symbolTable, inferenceProcessor)
                newPoints.add(it)
            }
        }

        // that's a crutch, but creation shouldn't be added to inference. Inference should prohibit `new` statements
        // to prevent implicit object creation. Implicit object creation is bad.
        if (alreadyCreatedPoints.size == 2) {
            TheoremParser.check(
                BinaryNotEquals(
                    ArithmeticExpr(mutableMapOf(PointNotation(alreadyCreatedPoints.first()) to 1.0)),
                    ArithmeticExpr(mutableMapOf(PointNotation(alreadyCreatedPoints.last()) to 1.0))
                ), symbolTable
            )
            Relation.makeRelation(
                BinaryNotEquals(
                    ArithmeticExpr(mutableMapOf(PointNotation(alreadyCreatedPoints.first()) to 1.0)),
                    ArithmeticExpr(mutableMapOf(PointNotation(newPoints.first()) to 1.0))
                ), symbolTable, inferenceProcessor
            )
            Relation.makeRelation(
                BinaryNotEquals(
                    ArithmeticExpr(mutableMapOf(PointNotation(alreadyCreatedPoints.last()) to 1.0)),
                    ArithmeticExpr(mutableMapOf(PointNotation(newPoints.first()) to 1.0))
                ), symbolTable, inferenceProcessor
            )
            Relation.makeRelation(
                BinaryNotIn(
                    PointNotation(newPoints.first()),
                    Point2Notation(alreadyCreatedPoints.first(), alreadyCreatedPoints.last())
                ), symbolTable, inferenceProcessor
            )
            return
        }

        val p1 = PointNotation(notation.p1)
        val p2 = PointNotation(notation.p2)
        val p3 = PointNotation(notation.p3)
        Relation.makeRelation(
            BinaryNotEquals(
                ArithmeticExpr(mutableMapOf(p1 to 1.0)),
                ArithmeticExpr(mutableMapOf(p2 to 1.0)),
            ), symbolTable, inferenceProcessor
        )
        Relation.makeRelation(
            BinaryNotEquals(
                ArithmeticExpr(mutableMapOf(p1 to 1.0)),
                ArithmeticExpr(mutableMapOf(p3 to 1.0))
            ), symbolTable, inferenceProcessor
        )
        Relation.makeRelation(
            BinaryNotEquals(
                ArithmeticExpr(mutableMapOf(p3 to 1.0)),
                ArithmeticExpr(mutableMapOf(p2 to 1.0))
            ), symbolTable, inferenceProcessor
        )
        Relation.makeRelation(BinaryNotIn(p1, Point2Notation(p2.p, p3.p)), symbolTable, inferenceProcessor)
    }

    private fun checkTriangles(symbolTable: SymbolTable): Boolean {
        val notations = mutableListOf(
            TriangleNotation(notation.p1, notation.p3, notation.p2),
            TriangleNotation(notation.p2, notation.p1, notation.p3),
            TriangleNotation(notation.p2, notation.p3, notation.p1),
            TriangleNotation(notation.p3, notation.p1, notation.p2),
            TriangleNotation(notation.p3, notation.p2, notation.p1)
        )
        return notations.any { symbolTable.hasTriangle(it) }
    }
}
