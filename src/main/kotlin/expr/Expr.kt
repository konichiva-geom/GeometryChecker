package expr

import PointCollection
import Relation
import Signature
import SymbolTable
import Utils
import Utils.lambdaToSign
import Utils.mergeWithOperation
import entity.LineRelations

/**
 * Expression that returns some value, e.g. [BinaryIntersects] returns point, or segment, or something else
 */
interface Returnable {
    fun getReturnValue(): Any
}

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
    // TODO check intersects for all except points
    override fun check(symbolTable: SymbolTable): Boolean {
        if (left is PointNotation && right is Point2Notation) {
            // A in ray AB
            if ((right.p1 == left.p || right.p2 == left.p))
                return true
            // A == C; C in AB
            val pointEntity = symbolTable.getPoint(left)
            if (symbolTable.getPoint(right.p1) == pointEntity || symbolTable.getPoint(right.p2) == pointEntity)
                return true
            // point in circle
        } else if (left is PointNotation && right is IdentNotation) {
            val point = symbolTable.getPoint(left)
            return symbolTable.getCircle(right).points
                .map { symbolTable.getPoint(it) }.toSet().contains(point)
            // arc in arc, point in arc
        } else if (right is ArcNotation) {
            val pointLetters = (left as Point2Notation).getLetters()
            return symbolTable.getPointSetNotationByNotation(right).containsAll(pointLetters)
        }
        return BinaryIntersects(left as Notation, right as Notation).check(symbolTable)
    }

    override fun make(symbolTable: SymbolTable) {
        if (right is IdentNotation)
            symbolTable.getCircle(right).points.add((left as PointNotation).p)
        val pointList = if (left is PointNotation) listOf(left.p) else (left as Point2Notation).getLetters()
        val (collection, _) = symbolTable.getKeyValueByNotation(right as Point2Notation)
        (collection as PointCollection).addPoints(pointList)
    }

    override fun toString(): String {
        return "$left in $right"
    }
}

class BinaryIntersects(left: Notation, right: Notation) : BinaryExpr(left, right), Returnable {
    lateinit var intersectionValue: Any // two circles intersect by array of points
    override fun getReturnValue(): Any = intersectionValue

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
        val leftSet = symbolTable.getPointSetNotationByNotation(left as Notation)
        val rightSet = symbolTable.getPointSetNotationByNotation(right as Notation)
        val intersection = leftSet.intersect(rightSet)
        if (intersection.isNotEmpty()) {
            // TODO: maybe validate that all intersection points are the same object
            intersectionValue = PointNotation(intersection.first())
            return
        }
        val intersectionPoint = Utils.NameGenerator.getName()
    }
}

class BinaryParallel(left: Point2Notation, right: Point2Notation) : BinaryExpr(left, right) {
    override fun toString(): String {
        return "$left || $right"
    }

    /**
     * Check all notations in [left] parallel set to see if one of them corresponds to the [right] [LineRelations]
     */
    override fun check(symbolTable: SymbolTable): Boolean {
        val line1 = (left as Point2Notation).toLine()
        val expectedNotation = (symbolTable.getRelationsByNotation((right as Point2Notation).toLine()) as LineRelations)
        for (lineNotation in (symbolTable.getRelationsByNotation(line1) as LineRelations).parallel) {
            if (symbolTable.getLine(lineNotation) == expectedNotation)
                return true
        }
        return false
    }

    override fun make(symbolTable: SymbolTable) {
        val line1 = (left as Point2Notation).toLine()
        val line2 = (right as Point2Notation).toLine()
        val lineRelations1 = symbolTable.getLine(line1)
        val lineRelations2 = symbolTable.getLine(line2)
        if (!lineRelations1.parallel.map { symbolTable.getLine(it) }.contains(lineRelations2))
            lineRelations1.parallel.add(line2)
        if (!lineRelations2.parallel.map { symbolTable.getLine(it) }.contains(lineRelations1))
            lineRelations1.parallel.add(line1)
    }
}

class BinaryPerpendicular(left: Point2Notation, right: Point2Notation) : BinaryExpr(left, right) {
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