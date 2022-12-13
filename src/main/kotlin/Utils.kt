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

object Utils {
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

    fun getRelationByString(tuple: Tuple3<Notation, TokenMatch, Notation>): BinaryExpr {
        return when (tuple.t2.text) {
            "in" -> BinaryIn(tuple.t1, tuple.t3)
            "intersects", "∩" -> BinaryIntersects(tuple.t1, tuple.t3)
            "parallel", "||" -> BinaryParallel(tuple.t1, tuple.t3)
            "perpendicular", "⊥" -> BinaryPerpendicular(tuple.t1, tuple.t3)
            else -> throw Exception("Unknown comparison")
        }
    }
}