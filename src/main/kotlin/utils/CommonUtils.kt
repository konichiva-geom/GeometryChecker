package utils

import entity.expr.notation.NumNotation
import entity.expr.notation.Point2Notation
import entity.expr.notation.Point3Notation
import error.PosError
import error.SpoofError
import java.io.File

object CommonUtils {
    const val SHOULD_CATCH = false
    const val CONSIDERED_DIGITS_AFTER_POINT = 6
    const val THEOREMS_PATH = "src/main/resources/theorems.txt"
    const val INFERENCE_PATH = "src/main/resources/inference.txt"
    val keyForArithmeticNumeric = NumNotation(0.0)

    val primes = File("primes.txt").readText().split(" ", "\n").map { it.toInt() }

    private val lambdas = mutableListOf({ a: Double, b: Double -> a + b },
        { a: Double, b: Double -> a - b },
        { a: Double, b: Double -> a * b },
        { a: Double, b: Double -> a / b })

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

    fun isSame(a: Any, b: Any): Boolean {
        return a === b
    }

    fun catchWithRange(block:() -> Any, range: IntRange): Any {
        return if (SHOULD_CATCH)
            try {
                block()
            } catch (e: SpoofError) {
                e.toString()
                throw PosError(range, e.msg, *e.args)
            }
        else
            block()
    }

    fun sortPoints(vararg points: String): List<String> {
        return points.sortedDescending()
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
