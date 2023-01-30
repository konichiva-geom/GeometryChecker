package expr

import SpoofError
import SymbolTable
import Utils.max
import Utils.min
import Utils.sortAngle
import Utils.sortLine
import entity.AngleRelations
import entity.CircleRelations
import pipeline.interpreter.ExpressionMapper

interface Renamable {
    /**
     * Remove key from map, rename key and put it back into map.
     * This procedure will put it into cell corresponding to its new hashcode,
     * just renaming won't work
     */
    fun renameAndRemap(symbolTable: SymbolTable)
    fun checkValidityAfterRename()
}

/**
 * Represents some structure of points (angle, line e.t.c) or te name of a circle
 * Order:
 * Num: 0
 * Point: 1
 * Segment: 2
 * Ray: 3
 * Arc: 4
 * Point2 (Line): 5
 * Point3: 6
 * Ident: 7
 */
abstract class Notation : Expr, Comparable<Expr>, Renamable {
    abstract fun getOrder(): Int
    fun compareOrSame(other: Expr): Int? {
        if (other is Notation && other.getOrder() != this.getOrder())
            return getOrder().compareTo(other.getOrder())
        return null
    }

    override fun getChildren(): List<Expr> = listOf()

    abstract fun getLetters(): MutableList<String>

    abstract fun mergeMapping(mapper: ExpressionMapper, other: Notation)
    abstract fun createLinks(mapper: ExpressionMapper)

    override fun equals(other: Any?): Boolean = toString() == other.toString()
    override fun hashCode(): Int = toString().hashCode()

    override fun toString(): String = getLetters().joinToString(separator = "")

    // TODO rewrite in case [RectangleNotation] is created
    override fun checkValidityAfterRename() {
        if (getLetters().size == 3 && getLetters().toSet().size != 3) {
            val names = if (getLetters()[0] == getLetters()[1]) mutableListOf(1, 2, getLetters()[0])
            else if (getLetters()[0] == getLetters()[1]) mutableListOf(1, 3, getLetters()[0])
            else mutableListOf(2, 3, getLetters()[1])
            throw SpoofError(
                "Cannot use notation with same points. %{first} and %{second} points equal to %{point}",
                "first" to names[0], "second" to names[1], "point" to names[2]
            )
        } else if (getLetters().size == 2 && getLetters().size == 1)
            throw SpoofError(
                "Cannot use notation with same points. %{first} and %{second} points equal to %{point}",
                "first" to getLetters()[0], "second" to getLetters()[1], "point" to getLetters()[0]
            )
    }
}

abstract class RelatableNotation : Notation()

class Point3Notation(var p1: String, var p2: String, var p3: String) : RelatableNotation() {
    init {
        sortAngle(this)
    }

    override fun getOrder(): Int = 6

    override fun compareTo(other: Expr): Int = super.compareOrSame(other) ?: toString().compareTo(other.toString())

    override fun getRepr() = StringBuilder("AAA")
    override fun mapIdents(mapper: ExpressionMapper) = Point3Notation(mapper.get(p1), mapper.get(p2), mapper.get(p3))
    override fun toString(): String = "$p1$p2$p3"

    override fun getLetters(): MutableList<String> = mutableListOf(p1, p2, p3)
    override fun mergeMapping(mapper: ExpressionMapper, other: Notation) {
        other as Point3Notation
        mapper.mergeMapping(p1, listOf(other.p1, other.p3))
        mapper.mergeMapping(p3, listOf(other.p1, other.p3))
        mapper.mergeMapping(p2, listOf(other.p2))
    }

    override fun createLinks(mapper: ExpressionMapper) {
        mapper.addLink(p1, p3)
    }

    override fun renameAndRemap(symbolTable: SymbolTable) {
        var relations: AngleRelations? = null
        if (symbolTable.angles[this] != null) {
            relations = symbolTable.angles[this]
            symbolTable.angles.remove(this)
        }

        p1 = symbolTable.identRenamer.getIdentical(p1)
        p2 = symbolTable.identRenamer.getIdentical(p2)
        p3 = symbolTable.identRenamer.getIdentical(p3)
        sortAngle(this)

        if (relations != null)
            symbolTable.angles[this] = relations
    }
}

open class Point2Notation(p1: String, p2: String) : RelatableNotation() {
    var p1: String
    var p2: String

    /**
     * cannot call sortLine() here, because  it will be called inside RayNotation constructor too,
     * destroying ray structure
     */
    init {
        this.p1 = min(p1, p2)
        this.p2 = max(p1, p2)
    }

    override fun getOrder(): Int = 5

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun getRepr() = StringBuilder("line AA")
    override fun mapIdents(mapper: ExpressionMapper) = Point2Notation(mapper.get(p1), mapper.get(p2))
    override fun toString(): String = "line $p1$p2"
    override fun getLetters(): MutableList<String> = mutableListOf(p1, p2)
    override fun mergeMapping(mapper: ExpressionMapper, other: Notation) {
        other as Point2Notation
        mapper.mergeMapping(p1, other.getLetters())
        mapper.mergeMapping(p2, other.getLetters())
    }

    override fun createLinks(mapper: ExpressionMapper) {
        mapper.addLink(p1, p2)
    }

    fun toRayNotation() = RayNotation(p1, p2)
    fun toSegmentNotation() = SegmentNotation(p1, p2)
    open fun toLine() = this

    override fun renameAndRemap(symbolTable: SymbolTable) {
        p1 = symbolTable.identRenamer.getIdentical(p1)
        p2 = symbolTable.identRenamer.getIdentical(p2)
        sortLine(this)
    }
}

class PointNotation(var p: String) : RelatableNotation() {
    override fun getOrder(): Int = 1

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun getRepr() = StringBuilder("A")
    override fun mapIdents(mapper: ExpressionMapper) = PointNotation(mapper.get(p))
    override fun renameAndRemap(symbolTable: SymbolTable) {
        p = symbolTable.identRenamer.getIdentical(p)
    }

    override fun toString(): String = p
    override fun getLetters(): MutableList<String> = mutableListOf(p)
    override fun mergeMapping(mapper: ExpressionMapper, other: Notation) {
        other as PointNotation
        mapper.mergeMapping(p, listOf(other.p))
    }

    override fun createLinks(mapper: ExpressionMapper) {}
}

class RayNotation(p1: String, p2: String) : Point2Notation(p1, p2) {
    init {
        this.p1 = p1
        this.p2 = p2
    }

    override fun mergeMapping(mapper: ExpressionMapper, other: Notation) {
        other as RayNotation
        mapper.mergeMapping(p1, listOf(other.p1))
        mapper.mergeMapping(p2, listOf(other.p2))
    }

    override fun createLinks(mapper: ExpressionMapper) {}

    override fun getOrder(): Int = 3

    override fun toLine() = Point2Notation(p1, p2)
    override fun mapIdents(mapper: ExpressionMapper) = RayNotation(mapper.get(p1), mapper.get(p2))

    override fun getRepr() = StringBuilder("ray AA")
    override fun toString(): String = "ray ${super.toString()}"

    override fun renameAndRemap(symbolTable: SymbolTable) {
        p1 = symbolTable.identRenamer.getIdentical(p1)
        p2 = symbolTable.identRenamer.getIdentical(p2)
    }
}

class SegmentNotation(p1: String, p2: String) : Point2Notation(p1, p2) {
    override fun getOrder(): Int = 2

    override fun toLine() = Point2Notation(p1, p2)
    override fun getRepr() = StringBuilder("AA")
    override fun mapIdents(mapper: ExpressionMapper) = SegmentNotation(mapper.get(p1), mapper.get(p2))
    override fun toString(): String = "$p1$p2"
}

class ArcNotation(p1: String, p2: String, var circle: String) : Point2Notation(p1, p2) {
    override fun getOrder(): Int = 4
    override fun toLine() = Point2Notation(p1, p2)
    override fun getRepr() = StringBuilder("arc AA")
    override fun mapIdents(mapper: ExpressionMapper) = ArcNotation(mapper.get(p1), mapper.get(p2), mapper.get(circle))
    override fun toString(): String = "arc ${super.toString()} of $circle"
    override fun renameAndRemap(symbolTable: SymbolTable) {
        super.renameAndRemap(symbolTable)
        circle = symbolTable.identRenamer.getIdentical(circle)
    }
}

class IdentNotation(private var text: String) : RelatableNotation() {
    override fun getOrder(): Int = 7
    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun getRepr() = StringBuilder("c")
    override fun mapIdents(mapper: ExpressionMapper) = IdentNotation(mapper.get(text))
    override fun toString(): String = text
    override fun getLetters(): MutableList<String> = mutableListOf(text)
    override fun mergeMapping(mapper: ExpressionMapper, other: Notation) {
        mapper.mergeMapping(text, listOf((other as IdentNotation).text))
    }

    override fun createLinks(mapper: ExpressionMapper) {}

    override fun renameAndRemap(symbolTable: SymbolTable) {
        var circleRelations: CircleRelations? = null
        if (symbolTable.circles[this] != null) {
            circleRelations = symbolTable.circles[this]
            symbolTable.circles.remove(this)
        }

        text = symbolTable.identRenamer.getIdentical(text)

        if (circleRelations != null)
            symbolTable.circles[this] = circleRelations
    }
}

class NumNotation(val number: Number) : Notation() {
    override fun getOrder(): Int = 0
    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun getRepr() = StringBuilder("0")
    override fun mapIdents(mapper: ExpressionMapper) = NumNotation(number)
    override fun renameAndRemap(symbolTable: SymbolTable) {}

    override fun toString(): String = number.toString()
    override fun getLetters(): MutableList<String> = mutableListOf()

    override fun mergeMapping(mapper: ExpressionMapper, other: Notation) {}
    override fun createLinks(mapper: ExpressionMapper) {}
}