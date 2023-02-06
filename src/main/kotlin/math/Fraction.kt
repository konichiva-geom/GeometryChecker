package math

import utils.MathUtils.getGCD
import utils.MathUtils.getLCM

/**
 * Class for calculation without rounding errors.
 * First element is numerator, second is denominator
 */
typealias Fraction = IntArray

private fun toCommonDenominator(first: Fraction, second: Fraction): Triple<Int, Int, Int> {
    val lcm = getLCM(first[1], second[1])
    val firstTerm = lcm / first[1] * first[0]
    val secondTerm = lcm / second[1] * second[0]
    return Triple(firstTerm, secondTerm, lcm)
}

fun Fraction.reduce() {
    val gcd = getGCD(this[0], this[1])
    this[0] /= gcd
    this[1] /= gcd
}

fun Fraction.unaryMinus(): Fraction {
    this[0] = -this[0]
    return this
}

fun Fraction.isZero() = this[0] == 0

operator fun Fraction.plus(other: Fraction): Fraction {
    val (first, second, lcm) = toCommonDenominator(this, other)
    return FractionFactory.create(first + second, lcm)
}

operator fun Fraction.minus(other: Fraction): Fraction {
    val (first, second, lcm) = toCommonDenominator(this, other)
    return FractionFactory.create(first - second, lcm)
}

operator fun Fraction.times(other: Fraction) = FractionFactory.create(this[0] * other[0], this[1] * other[1])

operator fun Fraction.div(other: Fraction) = FractionFactory.create(this[0] * other[1], this[1] * other[0])

fun Fraction.inPlaceDiv(other: Fraction) {
    this[0] *= other[1]
    this[1] *= other[0]
}

fun Fraction.inPlaceMul(other: Fraction) {
    this[0] *= other[0]
    this[1] *= other[1]
}

fun Fraction.compareTo(other: Fraction): Int {
    val (first, second, _) = toCommonDenominator(this, other)
    return first.compareTo(second)
}

fun Fraction.asString(): String {
    if (this[1] != 1)
        return "${this[0]}/${this[1]}"
    return when (this[0]) {
        0 -> "0"
        1 -> ""
        -1 -> "-"
        else -> this[0].toString()
    }
}

fun Fraction.isNegative() = this[0] < 0

object FractionFactory {
    fun zero() = intArrayOf(0, 1)
    fun one() = intArrayOf(1, 1)
    fun fromInt(value: Int) = intArrayOf(value, 1)
    fun create(numerator: Int, denominator: Int) = intArrayOf(numerator, denominator)
}
