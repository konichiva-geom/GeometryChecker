package utils

import entity.expr.notation.NumNotation
import entity.expr.notation.Point2Notation
import entity.expr.notation.Point3Notation
import error.PosError
import error.SpoofError
import math.*

object Utils {
    private const val SHOULD_CATCH = false
    const val THEOREMS_PATH = "examples/theorems.txt"
    const val INFERENCE_PATH = "examples/inference.txt"
    val keyForArithmeticNumeric = NumNotation(FractionFactory.zero())

    private val lambdas = mutableListOf({ a: Fraction, b: Fraction -> a.add(b) },
        { a: Fraction, b: Fraction -> a.subtract(b) },
        { a: Fraction, b: Fraction -> a.multiply(b) },
        { a: Fraction, b: Fraction -> a.divide(b) })

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
     * It is guaranteed that String is not empty, no IndexOutOfBoundsException
     */
    fun String.isPoint(): Boolean = substring(0, 1).uppercase() == substring(0, 1)
}
