package entity.expr.notation

import entity.Renamable
import entity.expr.Expr
import error.SpoofError
import pipeline.interpreter.IdentMapper

/**
 * Represents some structure of points (angle, line e.t.c) or the name of a circle
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

    abstract fun getPointsAndCircles(): MutableList<String>

    abstract fun mergeMapping(mapper: IdentMapper, other: Notation)
    abstract fun createLinks(mapper: IdentMapper)

//    override fun equals(other: Any?): Boolean = toString() == other.toString()
//    override fun hashCode(): Int = toString().hashCode()

    override fun hashCode(): Int {
        return getPointsAndCircles().sorted().joinToString(separator = "").hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if(other == null)
            return false
        if (this::class != other::class)
            return false
        return getPointsAndCircles().sorted().joinToString(separator = "") ==
                (other as Notation).getPointsAndCircles().sorted().joinToString(separator = "")
    }

    override fun toString(): String = getPointsAndCircles().joinToString(separator = "")

    // TODO rewrite in case [RectangleNotation] is created
    override fun checkValidityAfterRename(): Exception? {
        if (getPointsAndCircles().size == 3 && getPointsAndCircles().toSet().size != 3) {
            val names = if (getPointsAndCircles()[0] == getPointsAndCircles()[1]) mutableListOf(1, 2, getPointsAndCircles()[0])
            else if (getPointsAndCircles()[0] == getPointsAndCircles()[1]) mutableListOf(1, 3, getPointsAndCircles()[0])
            else mutableListOf(2, 3, getPointsAndCircles()[1])
            return SpoofError(
                "Cannot use notation with same points. %{first} and %{second} points equal to %{point}",
                "first" to names[0], "second" to names[1], "point" to names[2]
            )
        } else if (getPointsAndCircles().size == 2 && getPointsAndCircles().toSet().size == 1)
            return SpoofError(
                "Cannot use notation with same points. %{first} and %{second} points equal to %{point}",
                "first" to getPointsAndCircles()[0],
                "second" to getPointsAndCircles()[1],
                "point" to getPointsAndCircles()[0]
            )
        return null
    }
}

abstract class RelatableNotation : Notation()
