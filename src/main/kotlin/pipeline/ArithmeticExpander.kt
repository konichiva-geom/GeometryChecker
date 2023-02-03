package pipeline

import entity.expr.notation.Notation
import entity.expr.notation.NumNotation
import error.SpoofError
import math.*
import utils.ExtensionUtils.addOrCreate
import utils.MathUtils

object ArithmeticExpander {
    fun createArithmeticMap(
        left: MutableMap<Notation, Fraction>,
        right: MutableMap<Notation, Fraction>,
        op: String
    ): MutableMap<Notation, Fraction> {
        when (op) {
            "+", "-" -> return left.mergeWithOperation(right, op)
            "*" -> {
                val res = mutableMapOf<Notation, Fraction>()
                for ((key, value) in left) {
                    for ((key2, value2) in right) {
                        val (max, min) = MathUtils.maxToMin(Notation::getOrder, key, key2)
                        res[mulBy(max, min)] = value * value2
                    }
                }
                return res
            }
            "/" -> {
                return mutableMapOf(DivNotation(left, right) to FractionFactory.one())
            }
        }
        throw SpoofError("Unexpected operator")
    }

    fun mulBy(first: Notation, second: Notation): Notation {
        if (first is DivNotation && second is DivNotation) {
            return DivNotation(
                createArithmeticMap(first.numerator, second.numerator, "*"),
                createArithmeticMap(first.denominator, second.denominator, "*")
            )
        } else if (first is MulNotation && second is MulNotation) {
            return MulNotation((first.elements + second.elements).toMutableList())
        } else if (first is NumNotation && second is NumNotation) {
            return first
        } else if (first is DivNotation && second is MulNotation) {
            return DivNotation(
                first.numerator.entries.associate {
                    val (max, min) = MathUtils.maxToMin(Notation::getOrder, it.key, second)
                    mulBy(max, min) to it.value
                }.toMutableMap(),
                first.denominator.toMutableMap()
            )
        } else if (first is DivNotation && second is NumNotation) {
            return DivNotation(first.numerator.toMutableMap(), first.denominator.toMutableMap())
        } else if (first is DivNotation) {
            return DivNotation(
                first.numerator.toMutableMap().addOrCreate(second, FractionFactory.one()),
                first.denominator.toMutableMap()
            )
        } else if (first is MulNotation && second is NumNotation) {
            return MulNotation(first.elements.toMutableList())
        } else if (first is MulNotation) {
            return MulNotation((first.elements + second).toMutableList())
        } else if (second is NumNotation) {
            return first
        } else {
            return MulNotation(mutableListOf(first, second))
        }
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
                    divNotationFromNumeratorDivAndMapDenominator(
                        mergeMapToDivNotation(notation.numerator),
                        notation.denominator
                    )
                else notationFlattened
                // there is a bug if map is mutableMapOf(DivNotation(3, 7) -> Fraction(...))
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
        if(denominator.isEmpty())
            denominator = mutableMapOf(NumNotation(FractionFactory.zero()) to FractionFactory.one())
        return DivNotation(numerator, denominator)
    }

    private fun divNotationFromNumeratorDivAndMapDenominator(
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

    fun getArithmeticToString(map: MutableMap<Notation, Fraction>): String {
        if(map.isEmpty())
            return "0"
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
}