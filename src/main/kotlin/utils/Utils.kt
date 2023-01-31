import ExtensionUtils.toRange
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.utils.Tuple3
import entity.expr.notation.*
import expr.*
import math.Fraction
import math.div
import math.minus
import math.times
import java.io.File

object Utils {
    const val SHOULD_CATCH = true
    const val THEOREMS_PATH = "examples/theorems.txt"

    private val lambdas = mutableListOf({ a: Fraction, b: Fraction -> a + b },
        { a: Fraction, b: Fraction -> a - b },
        { a: Fraction, b: Fraction -> a * b },
        { a: Fraction, b: Fraction -> a / b })

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

    fun catchWithRangeAndArgs(block: () -> Any, range: IntRange, vararg args: Pair<String, Any>): Any {
        return if (SHOULD_CATCH)
            try {
                block()
            } catch (e: SpoofError) {
                throw PosError(range, e.msg, *e.args, *args)
            }
        else
            block()
    }

    fun sortLine(notation: Point2Notation): Point2Notation {
        if (notation.p1 == notation.p2)
            throw SpoofError("Line %{line} consists of same points", "line" to notation)
        if (notation.p1 > notation.p2)
            notation.p1 = notation.p2.also { notation.p2 = notation.p1 }
        return notation
    }

    fun sortAngle(notation: Point3Notation): Point3Notation {
        if (notation.p1 > notation.p3)
            notation.p1 = notation.p3.also { notation.p3 = notation.p1 }
        return notation
    }

    /**
     * Create relation binary expression
     */
    fun getBinaryRelationByString(tuple: Tuple3<Notation, TokenMatch, Notation>): BinaryExpr {
        return catchWithRangeAndArgs({
            val first = tuple.t1
            val operator = tuple.t2.text
            val second = tuple.t3
            when (operator) {
                "in" -> {
                    checkNotNumber(first, operator)
                    checkNotNumber(second, operator)
                    checkNotCircle(first, operator)
                    checkNotPoint(second, operator)
                    checkNotAngle(first, operator)
                    checkNotAngle(second, operator)
                    if (first is ArcNotation && second !is ArcNotation)
                        throw SpoofError("If arc is at the first position in `in`, then it should be in the second position too")
                    if (second is ArcNotation && first !is PointNotation)
                        throw SpoofError("If arc is at the second position in `in`, then point or arc should be in the first position")
                    checkNoGreaterOrder(first, second)
                    BinaryIn(first, second)
                }
                "intersects", "∩" -> {
                    checkNotNumber(first, operator)
                    checkNotNumber(second, operator)
                    checkNotPoint(first, operator)
                    checkNotPoint(second, operator)
                    checkNotAngle(first, operator)
                    checkNotAngle(second, operator)
                    BinaryIntersects(first, second)
                }

                "parallel", "||" -> {
                    checkLinear(first, second, operator)
                    BinaryParallel(first as Point2Notation, second as Point2Notation)
                }

                "perpendicular", "⊥" -> {
                    checkLinear(first, second, operator)
                    BinaryPerpendicular(first as Point2Notation, second as Point2Notation)
                }

                else -> throw SystemFatalError("Unknown comparison")
            }
        }, tuple.t2.toRange()) as BinaryExpr
    }

    private fun checkNoGreaterOrder(first: Notation, second: Notation) {
        if (first.getOrder() > second.getOrder())
            throw SpoofError("`$first` is 'smaller' than `$second`")
    }

    private fun checkNotNumber(notation: Notation, operator: String) {
        if (notation is NumNotation
        )
            throw SpoofError("`$notation` is number, `$operator` is not applicable to numbers")
    }

    private fun checkNotPoint(notation: Notation, operator: String) {
        if (notation is PointNotation)
            throw SpoofError("`$notation` is point, `$operator` is not applicable to points in this position")
    }

    private fun checkNotAngle(notation: Notation, operator: String) {
        if (notation is Point3Notation)
            throw SpoofError("`$notation` is angle, `$operator` is not applicable to angle in this position")
    }

    private fun checkNotCircle(notation: Notation, operator: String) {
        if (notation is IdentNotation)
            throw SpoofError("`$notation` is circle, `$operator` is not applicable to circle in this position")
    }

    private fun checkLinear(first: Notation, second: Notation, operator: String) {
        if (first !is Point2Notation || second !is Point2Notation || first is ArcNotation || second is ArcNotation)
            throw SpoofError(
                "`${if (first !is Point2Notation) first else second}` is not linear, `$operator` is not applicable"
            )
    }

    /**
     * It is guaranteed that String is not empty, no IndexOutOfBoundsException
     */
    fun String.isPoint(): Boolean = substring(0, 1).uppercase() == substring(0, 1)

    object NameGenerator {
        private var index = 0

        fun getName() = "$${index++}"
    }

    /**
     * Returns next prime number for comparison vectors
     */
    object PrimeGetter {
        val primes = File("src/main/resources/primes.txt").readText().split(" ", "\n").map { it.toInt() }
    }
}
