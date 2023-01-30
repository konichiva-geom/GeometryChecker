package relations

import SymbolTable
import SystemFatalError
import Utils.addOrCreate
import Utils.signToLambda
import expr.Notation

class Vector(
    private val value: MutableMap<Int, Float>
) : MutableMap<Int, Float> {
    constructor(newVariable: Int) : this(mutableMapOf(newVariable to 1f))

    override val size: Int = value.size
    override val entries: MutableSet<MutableMap.MutableEntry<Int, Float>> = value.entries
    override val keys: MutableSet<Int> = value.keys
    override val values: MutableCollection<Float> = value.values
    override fun containsKey(key: Int): Boolean = value.containsKey(key)
    override fun containsValue(value: Float): Boolean = this.value.containsValue(value)
    override fun get(key: Int): Float? = value[key]
    override fun clear() = value.clear()
    override fun isEmpty(): Boolean = value.isEmpty()
    override fun remove(key: Int): Float? = value.remove(key)
    override fun putAll(from: Map<out Int, Float>) = value.putAll(from)
    override fun put(key: Int, value: Float): Float? = this.value.put(key, value)

    /**
     * Merge current vectors by addition, subtraction or multiplication
     */
    fun merge(other: Vector, operation: String): Vector {
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
                    val res = mutableMapOf<Int, Float>()
                    this.value.forEach { (thisKey, thisElement) ->
                        other.value.forEach { (otherKey, otherElement) ->
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
        return Vector(map)
    }

    fun multiplyBy(coeff: Float) {
        for ((key, number) in value) {
            value[key] = number * coeff
        }
    }

    fun mergeWithOperation(
        other: Vector,
        operation: String
    ): MutableMap<Int, Float> {
        return (keys + other.keys)
            .associateWith { signToLambda[operation]!!(this[it] ?: 0f, other[it] ?: 0f) }
            .toMutableMap()
    }

    private fun numericFirst(other: Vector): Pair<Float, Vector> {
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
    fun compare(other: Vector): Int {
        val res = mutableMapOf(1 to false, -1 to false, 0 to false)
        value.keys.forEach {
            if (other.value[it] == null)
                res[1] = true
            else
                res[value[it]!!.compareTo(other.value[it]!!)] = true
        }
        if (res.values.count { it } >= 2)
            return 2
        for ((key, value) in res)
            if (value)
                return key
        return 0
    }

    override fun toString(): String {
        return value.toString()
    }

    companion object {
        /**
         * Create vector and add it to the symbol table
         */
        fun fromNotation(symbolTable: SymbolTable, notation: Notation): Vector {

            return symbolTable.getOrCreateVector(notation)
        }
    }
}