import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.utils.Tuple3
import expr.BinaryExpr
import expr.BinaryIn
import expr.BinaryIntersects
import expr.BinaryParallel
import expr.BinaryPerpendicular
import expr.Notation
import expr.Point2Notation
import expr.Point3Notation
import expr.PointNotation

object Utils {
    const val THEOREMS_PATH = "examples/theorems.txt"

    private val lambdas = mutableListOf({ a: Float, b: Float -> a + b },
        { a: Float, b: Float -> a - b },
        { a: Float, b: Float -> a * b },
        { a: Float, b: Float -> a / b })
    val signToLambda = mutableMapOf(
        "+" to lambdas[0],
        "-" to lambdas[1],
        "*" to lambdas[2],
        "/" to lambdas[3]
    )
    val lambdaToSign = mutableMapOf(
        lambdas[0] to "+",
        lambdas[1] to "-",
        lambdas[2] to "*",
        lambdas[3] to "/"
    )

    fun catchWithArgs(block: () -> Any, vararg args: Pair<String, Any>): Any {
        try {
            return block()
        } catch (e: Exception) {
            throw SpoofError(e.message!!, *args)
        }
    }

    fun sortLine(notation: Point2Notation): Point2Notation {
        if (notation.p1 == notation.p2) throw SpoofError("Line %{line} consists of same points", "line" to notation)
        if (notation.p1 > notation.p2)
            notation.p1 = notation.p2.also { notation.p2 = notation.p1 }
        return notation
    }

    fun sortAngle(notation: Point3Notation): Point3Notation {
        if (notation.p1 > notation.p3)
            notation.p1 = notation.p3.also { notation.p3 = notation.p1 }
        return notation
    }

    fun <T> MutableMap<T, Float>.mergeWithOperation(
        other: MutableMap<T, Float>,
        operation: (Float, Float) -> Float
    ): MutableMap<T, Float> {
        return (keys + other.keys)
            .associateWith { operation(this[it] ?: 0f, other[it] ?: 0f) }
            .toMutableMap()
    }

    /**
     * Create relation binary expression
     */
    fun getBinaryRelationByString(tuple: Tuple3<Notation, TokenMatch, Notation>): BinaryExpr {
        try {
            return when (tuple.t2.text) {
                "in" ->  {
                    checkNotPoint(tuple.t3, tuple.t2.text)
                    BinaryIn(tuple.t1, tuple.t3)
                }
                "intersects", "∩" ->{
                    checkNotPoint(tuple.t1, tuple.t2.text)
                    checkNotPoint(tuple.t3, tuple.t2.text)
                    BinaryIntersects(tuple.t1, tuple.t3)}
                "parallel", "||" -> {
                    checkLinear(tuple.t1, tuple.t3, tuple.t2.text)
                    BinaryParallel(tuple.t1 as Point2Notation, tuple.t3 as Point2Notation)
                }
                "perpendicular", "⊥" -> {
                    checkLinear(tuple.t1, tuple.t3, tuple.t2.text)
                    BinaryPerpendicular(tuple.t1 as Point2Notation, tuple.t3 as Point2Notation)
                }

                else -> throw Exception("Unknown comparison")
            }
        } catch (spoof: SpoofError) {
            throw PosError(tuple.t2.toRange(), spoof.msg)
        }
    }

    private fun checkNotPoint(notation: Notation, operator: String) {
        if(notation is PointNotation)
            throw SpoofError("`$notation` is point, `$operator` is not applicable to points in this position")
    }

    private fun checkLinear(first: Notation, second: Notation, operator: String) {
        if (first !is Point2Notation || second !is Point2Notation)
            throw SpoofError("`${if (first !is Point2Notation) first else second}` is not linear, `$operator` is not applicable")
    }

    fun <T : Comparable<T>?> max(t1: T, t2: T): T {
        return if (t1!! > t2) t1 else t2
    }

    fun <T : Comparable<T>?> min(t1: T, t2: T): T {
        return if (t1!! < t2) t1 else t2
    }

    object NameGenerator {
        var index = 0

        fun getName() = "$${index++}"
    }
}