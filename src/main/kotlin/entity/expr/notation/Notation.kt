package entity.expr.notation

import SpoofError
import entity.Renamable
import expr.Expr
import pipeline.interpreter.IdentMapper

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

    abstract fun mergeMapping(mapper: IdentMapper, other: Notation)
    abstract fun createLinks(mapper: IdentMapper)

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
