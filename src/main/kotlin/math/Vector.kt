package math

import entity.expr.notation.Notation
import error.SystemFatalError
import pipeline.SymbolTable
import utils.ExtensionUtils.addOrCreate
import utils.Utils.signToLambda

typealias Vector = MutableMap<Int, Fraction>

/**
 * Merge current vectors by addition, subtraction or multiplication
 */
fun Vector.mergeWith(other: Vector, operation: String): Vector {
    val map = when (operation) {
        "+", "-" -> mergeWithOperation(other, operation)
        "*" -> {
            // something is a constant numeric value
            if (this.size == 1 && this.keys.contains(0)
                || other.size == 1 && other.keys.contains(0)
            ) {
                val (numeric, vector) = numericFirst(other)
                vector.keys.associateWith { vector[it]!! * numeric }.toMutableMap()
            } else {
                val res = mutableMapOf<Int, Fraction>()
                this.forEach { (thisKey, thisElement) ->
                    other.forEach { (otherKey, otherElement) ->
                        res.addOrCreate(
                            thisKey * otherKey,
                            thisElement * otherElement
                        )
                    }
                }
                res
            }
        }

        "/" -> throw SystemFatalError("Divisions should be removed before interpretation")
        else -> throw SystemFatalError("Unknown operation `$operation`")
    }
    return map
}

fun Vector.multiplyBy(coeff: Fraction) {
    for ((key, number) in this) {
        this[key] = number * coeff
    }
}

fun <T> MutableMap<T, Fraction>.mergeWithOperation(
    other: MutableMap<T, Fraction>,
    operation: String
): MutableMap<T, Fraction> {
    return (keys + other.keys)
        .associateWith {
            signToLambda[operation]!!(
                this[it] ?: FractionFactory.zero(),
                other[it] ?: FractionFactory.zero()
            )
        }
        .toMutableMap()
}

private fun Vector.numericFirst(other: Vector): Pair<Fraction, Vector> {
    return if (this.size == 1 && this.keys.contains(0))
        this[0]!! to other
    else other[0]!! to this
}

/**
 * -1 for this smaller than other
 *
 * 0 for this equal to other
 *
 * 1 for this bigger than other
 *
 * 2 for this incomparable than other
 */
fun Vector.compare(other: Vector): Int {
    val res = mutableMapOf(1 to false, -1 to false, 0 to false)
    keys.forEach {
        if (other[it] == null)
            res[1] = true
        else
            res[this[it]!!.compareTo(other[it]!!)] = true
    }
    if (res.values.count { it } >= 2)
        return 2
    for ((key, value) in res)
        if (value)
            return key
    return 0
}

/**
 * Create vector and add it to the symbol table
 */
fun fromNotation(symbolTable: SymbolTable, notation: Notation): Vector {
    return symbolTable.getOrCreateVector(notation)
}

fun fromInt(number: Int): Vector {
    return mutableMapOf(number to FractionFactory.one())
}
