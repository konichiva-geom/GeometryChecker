package pipeline

import entity.expr.notation.Notation
import entity.expr.notation.NumNotation
import error.SpoofError
import math.*
import utils.ExtensionUtils.addOrCreate
import utils.MathUtils
import utils.MathUtils.getGCD
import utils.Utils.keyForArithmeticNumeric

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
            // second is Notation
            return DivNotation(
                createArithmeticMap(
                    first.numerator.toMutableMap(),
                    mutableMapOf(second to FractionFactory.one()), "*"
                ),
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
                // create a denominator from current div notation
                if (denominator.isEmpty()) {
                    denominator.putAll(notationFlattened.denominator)
                    // multiply all elements by fraction
                    numerator = createArithmeticMap(numerator, notationFlattened.denominator, "*")
                        .mergeWithOperation(
                            notationFlattened.numerator.keys
                                .associateWith { notationFlattened.numerator[it]!! * fraction }.toMutableMap(), "+"
                        )
                } else {
                    val first = createArithmeticMap(numerator, notationFlattened.denominator, "*")
                    val second = createArithmeticMap(
                        notationFlattened.numerator.keys.associateWith { notationFlattened.numerator[it]!! * fraction }
                            .toMutableMap(),
                        denominator,
                        "*"
                    )
                    numerator = first.mergeWithOperation(second, "+")
                    denominator = createArithmeticMap(notationFlattened.denominator, denominator, "*")
                }
            } else {
                numerator.addOrCreate(notation, fraction)
            }
        }
        removeZeros(numerator)
        removeZeros(denominator, isNumerator = false)
        return DivNotation(numerator, denominator)
    }

    fun simplifyTwoMaps(first: MutableMap<Notation, Fraction>, second: MutableMap<Notation, Fraction>) {
        first.forEach { it.value.reduce() }
        second.forEach { it.value.reduce() }

        val firstGCD = first.values.map { it[0] }.reduce { acc, i -> getGCD(acc, i) }
        val secondGCD = second.values.map { it[0] }.reduce { acc, i -> getGCD(acc, i) }

        val commonGCD = getGCD(firstGCD, secondGCD)
        assert(commonGCD != 0)
        if (commonGCD != 1) {
            first.forEach { it.value[0] /= commonGCD }
            second.forEach { it.value[0] /= commonGCD }
        }
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

    /**
     * TODO expand this method in future to check if the expression in denominator equals to zero => whole division is
     * incorrect
     */
    private fun checkForZero(denominator: MutableMap<Notation, Fraction>) {
        if (denominator.size == 1 && denominator[keyForArithmeticNumeric].contentEquals(FractionFactory.zero()))
            throw SpoofError("Expression leads to one with zero in denominator")
    }

    private fun removeZeros(map: MutableMap<Notation, Fraction>, isNumerator: Boolean = true) {
        if (!isNumerator)
            checkForZero(map)
        val mapWithoutZeros = map.filterValues { !it.isZero() }
        map.clear()
        map.putAll(mapWithoutZeros)
        if (map.isEmpty())
            map[keyForArithmeticNumeric] = if (isNumerator) FractionFactory.zero() else FractionFactory.one()
    }

    fun getArithmeticToString(map: MutableMap<Notation, Fraction>): String {
        if (map.isEmpty())
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