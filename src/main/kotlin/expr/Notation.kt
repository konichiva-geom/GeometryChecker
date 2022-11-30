package expr

abstract class Notation : Expr, Comparable<Expr>, Foldable {
    abstract fun getOrder(): Int
    fun compareOrSame(other: Expr): Int? {
        if (other is Notation && other.getOrder() != this.getOrder())
            return getOrder().compareTo(other.getOrder())
        return null
    }

    override fun getChildren(): List<Expr> {
        TODO("Not yet implemented")
    }

    abstract fun getLetters(): MutableList<String>

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
}

open class Point2Notation(var p1: String, var p2: String) : RelatableNotation() {
    override fun getOrder(): Int = 2

    override fun flatten(): MutableMap<Any, Float> {
        return mutableMapOf(this to 1f)
    }

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun toString(): String = "$p1$p2"
    override fun getLetters(): MutableList<String> = mutableListOf(p1, p2)

    fun toRayNotation() = RayNotation(p1, p2)
    fun toSegmentNotation() = SegmentNotation(p1, p2)
}

class PointNotation(val p: String) : RelatableNotation() {
    override fun getOrder(): Int = 1

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun toString(): String = p
    override fun getLetters(): MutableList<String> = mutableListOf(p)
}

class RayNotation(p1: String, p2: String) : Point2Notation(p1, p2)
class SegmentNotation(p1: String, p2: String) : Point2Notation(p1, p2)
class ArcNotation(p1: String, p2: String) : Point2Notation(p1, p2)
class IdentNotation(private val text: String) : RelatableNotation() {
    override fun getOrder(): Int = 0
    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun toString(): String = text
    override fun getLetters(): MutableList<String> = mutableListOf(text)
}

class NumNotation(val number: Number) : Notation() {
    override fun getOrder(): Int = -1
    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun toString(): String = number.toString()
    override fun getLetters(): MutableList<String> = mutableListOf()
}