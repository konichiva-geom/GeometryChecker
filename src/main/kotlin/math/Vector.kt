package math

import entity.expr.notation.Notation
import error.SystemFatalError
import pipeline.symbol_table.SymbolTable
import utils.ExtensionUtils.addOrCreate
import utils.MultiSet
import utils.Utils.signToLambda
import utils.multiSetOf

// TODO change set to multiset
typealias Vector = MutableMap<MultiSet<Int>, Double>


/**
 * Merge current vectors by addition, subtraction or multiplication
 */
fun Vector.mergeWith(other: Vector, operation: String): Vector {
    val map = when (operation) {
        "+", "-" -> mergeWithOperation(other, operation)
        "*" -> {
            // something is a constant numeric value
            if (this.size == 1 && this.keys.contains(multiSetOf(0))
                || other.size == 1 && other.keys.contains(multiSetOf(0))
            ) {
                val (numeric, vector) = numericFirst(other)
                vector.keys.associateWith { vector[it]!! * numeric }.toMutableMap()
            } else {
                val res = mutableMapOf<MultiSet<Int>, Double>()
                this.forEach { (thisKey, thisElement) ->
                    other.forEach { (otherKey, otherElement) ->
                        res.addOrCreate(
                            multiSetOf(*thisKey.toTypedArray(), *otherKey.toTypedArray()),
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

fun Vector.changeAllPairs(change: Pair<Int, Int>) {
    val oldKeys = keys.toSet()
    for (key in oldKeys) {
        if (key.contains(change.first)) {
            val value = remove(key)!!
            val newKey = key.toMutableMultiSet()
            newKey.remove(change.first)
            newKey.add(change.second)
            this[newKey.toMultiSet()] = value
        }
    }
}

fun Vector.copy(): Vector {
    return this.toMutableMap()
}

fun Vector.multiplyBy(coeff: Double): Vector {
    for ((key, number) in this) {
        this[key] = number * coeff
    }
    return this
}

fun <T> MutableMap<T, Double>.mergeWithOperation(
    other: MutableMap<T, Double>,
    operation: String
): MutableMap<T, Double> {
    return (keys + other.keys)
        .associateWith {
            signToLambda[operation]!!(
                this[it] ?: 0.0,
                other[it] ?: 0.0
            )
        }
        .filter { it.value != 0.0 } // TODO: should really delete zero values?
        .toMutableMap()
}

private fun Vector.numericFirst(other: Vector): Pair<Double, Vector> {
    return if (this.size == 1 && this.keys.contains(multiSetOf(0)))
        this[multiSetOf(0)]!! to other
    else other[multiSetOf(0)]!! to this
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

fun vectorFromArithmeticMap(map: MutableMap<Notation, Double>, symbolTable: SymbolTable): Vector {
    return map.keys.fold(
        mutableMapOf()
    ) { acc, notation ->
        map[notation]
        acc.mergeWithOperation(
            fromNotation(symbolTable, notation)
                .multiplyBy(map[notation]!!), "+"
        )
    }
}

/**
 * Create vector and add it to the symbol table
 */
fun fromNotation(symbolTable: SymbolTable, notation: Notation): Vector {
    return symbolTable.getOrCreateVector(notation)
}

fun fromInt(number: Int): Vector {
    return mutableMapOf(multiSetOf(number) to 1.0)
}

fun Vector.asString(): String {
    return this.entries.joinToString(separator = ", ") { "${it.key.reduce { acc, i -> acc * i }}:${it.value}" }
}