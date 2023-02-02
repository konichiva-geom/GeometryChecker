package utils

import GeomGrammar.createArithmeticMap
import entity.expr.notation.Notation
import entity.expr.notation.NumNotation
import entity.expr.notation.Point2Notation
import entity.expr.notation.Point3Notation
import error.PosError
import error.SpoofError
import math.*
import utils.ExtensionUtils.addOrCreate

object Utils {
    private const val SHOULD_CATCH = true
    const val THEOREMS_PATH = "examples/theorems.txt"
    val keyForArithmeticNumeric = NumNotation(FractionFactory.zero())

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
     * It is guaranteed that String is not empty, no IndexOutOfBoundsException
     */
    fun String.isPoint(): Boolean = substring(0, 1).uppercase() == substring(0, 1)

    fun getArithmeticToString(map: MutableMap<Notation, Fraction>): String {
        val res = StringBuilder()
        for ((notation, fraction) in map.entries.sortedBy { -it.key.getOrder() }) {
            if (fraction.isNegative())
                res.append(fraction.asString())
            else res.append("+${fraction.asString()}")
            res.append(notation)
        }
        if (res[0] == '+')
            res.deleteCharAt(0)
        return res.toString()
    }

    fun mergeMapToDivNotation(map: MutableMap<Notation, Fraction>): DivNotation {
        var numerator = mutableMapOf<Notation, Fraction>()
        var denominator = mutableMapOf<Notation, Fraction>()

        for ((notation, fraction) in map) {
            if (notation is DivNotation) {
                var notationFlattened = if (notation.denominator.keys.any { it is DivNotation })
                    divNotationFromDenominatorDivAndMapNumerator(
                        notation.numerator,
                        mergeMapToDivNotation(notation.denominator)
                    )
                else notation
                notationFlattened = if (notation.numerator.keys.any { it is DivNotation })
                    divNotationFromNumeratorDivAndMapDenumerator(
                        mergeMapToDivNotation(notation.numerator),
                        notation.denominator
                    )
                else notationFlattened
                if (denominator.isEmpty()) {
                    denominator.putAll(notationFlattened.denominator)
                    numerator = createArithmeticMap(numerator, notationFlattened.denominator, "*")
                        .mergeWithOperation(notationFlattened.numerator, "+")
                } else {
                    val first = createArithmeticMap(numerator, notationFlattened.denominator, "*")
                    val second = createArithmeticMap(notationFlattened.numerator, denominator, "*")
                    numerator = first.mergeWithOperation(second, "+")
                    denominator = createArithmeticMap(notationFlattened.denominator, denominator, "*")
                }
            } else {
                numerator.addOrCreate(notation, fraction)
            }
        }
        return DivNotation(numerator, denominator)
    }

    private fun divNotationFromNumeratorDivAndMapDenumerator(
        numerator: DivNotation,
        denominator: MutableMap<Notation, Fraction>
    ): DivNotation {
        val resDenominator = createArithmeticMap(numerator.denominator, denominator, "*")
        return DivNotation(numerator.numerator, resDenominator)
    }

    private fun divNotationFromDenominatorDivAndMapNumerator(
        numerator: MutableMap<Notation, Fraction>,
        denominator: DivNotation
    ): DivNotation {
        val resNumerator = createArithmeticMap(numerator, denominator.denominator, "*")
        return DivNotation(resNumerator, denominator.numerator)
    }
}
