package expr

import TheoremParser
import Utils.max
import Utils.min

abstract class Notation : Expr, Comparable<Expr>, Foldable {
    abstract fun getOrder(): Int
    fun compareOrSame(other: Expr): Int? {
        if (other is Notation && other.getOrder() != this.getOrder())
            return getOrder().compareTo(other.getOrder())
        return null
    }

    override fun getChildren(): List<Expr> = listOf()

    abstract fun getLetters(): MutableList<String>

    abstract fun mergeMapping(tp: TheoremParser, other: Notation)

    override fun equals(other: Any?): Boolean = toString() == other.toString()
    override fun hashCode(): Int = toString().hashCode()

    override fun toString(): String = getLetters().joinToString(separator = "")
}

abstract class RelatableNotation : Notation()

// class CoeffNotation(val coeff: Float, val notation: Notation) : Notation() {
//     override fun getOrder(): Int = 4
//
//     override fun compareTo(other: expr.Expr): Int {
//         TODO("Not yet implemented")
//     }
//
//     override fun getLetters(): List<String> {
//         TODO("Not yet implemented")
//     }
// }

// class MulNotation(private val elems: List<Notation>) : Notation() {
//     override fun compareTo(other: expr.Expr): Int {
//         // return super.compareOrSame(other) ?: {
//         // }
//         TODO("")
//     }
//
//     override fun getOrder(): Int = 5
//
//     override fun toString(): String = elems.joinToString(separator = "*")
// }

// class DivNotation(val top: Notation, val bottom: Notation) : Notation() {
//     override fun getOrder(): Int = 6
//
//     override fun toString(): String = "$top/$bottom"
//
//     override fun compareTo(other: expr.Expr): Int {
//         TODO("Not yet implemented")
//     }
// }

class Point3Notation(var p1: String, var p2: String, var p3: String) : RelatableNotation() {
    override fun getOrder(): Int = 3

    override fun flatten(): MutableMap<Any, Float> {
        return mutableMapOf(this to 1f)
    }

    override fun compareTo(other: Expr): Int = super.compareOrSame(other) ?: toString().compareTo(other.toString())

    override fun toString(): String = "$p1$p2$p3"

    override fun getLetters(): MutableList<String> = mutableListOf(p1, p2, p3)
    override fun mergeMapping(tp: TheoremParser, other: Notation) {
        other as Point3Notation
        tp.mergeMapping(p1, listOf(other.p1, other.p3))
        tp.mergeMapping(p3, listOf(other.p1, other.p3))
        tp.mergeMapping(p2, listOf(other.p2))
    }
}

open class Point2Notation(p1: String, p2: String) : RelatableNotation() {
    var p1: String
    var p2: String

    init {
        this.p1 = min(p1, p2)
        this.p2 = max(p1, p2)
    }

    override fun getOrder(): Int = 2

    override fun flatten(): MutableMap<Any, Float> {
        return mutableMapOf(this to 1f)
    }

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun toString(): String = "$p1$p2"
    override fun getLetters(): MutableList<String> = mutableListOf(p1, p2)
    override fun mergeMapping(tp: TheoremParser, other: Notation) {
        other as Point2Notation
        tp.mergeMapping(p1, other.getLetters())
        tp.mergeMapping(p2, other.getLetters())
    }

    fun toRayNotation() = RayNotation(p1, p2)
    fun toSegmentNotation() = SegmentNotation(p1, p2)
    open fun toLine() = this
}

class PointNotation(val p: String) : RelatableNotation() {
    override fun getOrder(): Int = 1

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun toString(): String = p
    override fun getLetters(): MutableList<String> = mutableListOf(p)
    override fun mergeMapping(tp: TheoremParser, other: Notation) {
        other as PointNotation
        tp.mergeMapping(p, listOf(other.p))
    }
}

class RayNotation(p1: String, p2: String) : Point2Notation(p1, p2) {
    init {
        this.p1 = p1
        this.p2 = p2
    }
    override fun mergeMapping(tp: TheoremParser, other: Notation) {
        other as RayNotation
        tp.mergeMapping(p1, listOf(other.p1))
        tp.mergeMapping(p2, listOf(other.p2))
    }

    override fun getOrder(): Int {
        return super.getOrder() + 1
    }

    override fun toLine() = Point2Notation(p1, p2)
}

class SegmentNotation(p1: String, p2: String) : Point2Notation(p1, p2) {
    override fun getOrder(): Int {
        return super.getOrder() + 2
    }

    override fun toLine() = Point2Notation(p1, p2)
}

class ArcNotation(p1: String, p2: String) : Point2Notation(p1, p2) {
    override fun toLine() = Point2Notation(p1, p2)
}
class IdentNotation(private val text: String) : RelatableNotation() {
    override fun getOrder(): Int = 0
    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun toString(): String = text
    override fun getLetters(): MutableList<String> = mutableListOf(text)
    override fun mergeMapping(tp: TheoremParser, other: Notation) {
        tp.mergeMapping(text, listOf((other as IdentNotation).text))
    }
}

class NumNotation(val number: Number) : Notation() {
    override fun getOrder(): Int = -1
    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun toString(): String = number.toString()
    override fun getLetters(): MutableList<String> = mutableListOf()

    override fun mergeMapping(tp: TheoremParser, other: Notation) {
        // do nothing
    }
}