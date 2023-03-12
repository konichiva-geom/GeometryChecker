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

fun Fraction.inPlaceUnaryMinus(): Fraction {
    this[0] = -this[0]
    return this
}

fun Fraction.unaryMinus(): Fraction {
    return FractionFactory.create(-this[0], this[1])
}

fun Fraction.isZero() = this[0] == 0

fun Fraction.add(other: Fraction): Fraction {
    val (first, second, lcm) = toCommonDenominator(this, other)
    return FractionFactory.create(first + second, lcm)
}

fun Fraction.subtract(other: Fraction): Fraction {
    val (first, second, lcm) = toCommonDenominator(this, other)
    return FractionFactory.create(first - second, lcm)
}

fun Fraction.multiply(other: Fraction) = FractionFactory.create(this[0] * other[0], this[1] * other[1])

fun Fraction.multiply(other: Int) = FractionFactory.create(this[0] * other, this[1])

fun Fraction.divide(other: Fraction) = FractionFactory.create(this[0] * other[1], this[1] * other[0])

fun Fraction.divide(other: Int) = FractionFactory.create(this[0], this[1] * other)

fun Fraction.inPlaceDiv(other: Fraction): Fraction {
    this[0] *= other[1]
    this[1] *= other[0]
    return this
}

fun Fraction.inPlaceMul(other: Fraction): Fraction {
    this[0] *= other[0]
    this[1] *= other[1]
    return this
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

fun Fraction.lessThanZero(): Boolean {
    return this[0] * this[1] < 0
}

fun Fraction.moreThanZero(): Boolean {
    return this[0] * this[1] > 0
}

fun Fraction.isNegative() = this[0] < 0

object FractionFactory {
    fun zero() = intArrayOf(0, 1)
    fun one() = intArrayOf(1, 1)
    fun fromInt(value: Int) = intArrayOf(value, 1)
    fun create(numerator: Int, denominator: Int) = intArrayOf(numerator, denominator)
}
