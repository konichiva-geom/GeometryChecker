import Utils.mergeWithOperation
import entity.EntityRelations
import expr.Notation

class VectorContainer<T : EntityRelations> {
    private val vectors = mutableMapOf<T, ComparisonVector>()
    private var maxDim = 0

    fun addNewVector(entity: T) {
        maxDim += 1
        vectors[entity] = ComparisonVector(mutableMapOf(maxDim to 1f)) // TODO `0 to 0f` is important?
    }
}

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
     * Merge current vectors by addition or subtraction
     */
    fun merge(other: Vector, operation: String): Vector {
        return Vector(value.mergeWithOperation(other.value, operation))
    }

    /**
     * Multiply or divide current vector by [other]
     */
    fun transform(other: Float, operation: (Float, Float) -> Float): Vector {
        return Vector(value.mapValues { operation(it.value, other) }.toMutableMap())
    }

    companion object {
        /**
         * Create vector and add it to the symbol table
         */
        fun fromNotation(symbolTable: SymbolTable, notation: Notation): Vector {
            val res = Vector(Utils.PrimeGetter.getNext())

            return res
        }
    }
}

class ComparisonVector(
    private val value: MutableMap<Int, Float>,
    private val left: MutableMap<Any, Float> = mutableMapOf(), // {"ABC": -2, 90: 1} is 90 - 2 * ABC
    private val right: MutableMap<Any, Float> = mutableMapOf()
) {
    /**
     * -1 for this smaller than other
     *
     * 0 for this equal to other
     *
     * 1 for this bigger than other
     *
     * 2 for this incomparable than other
     */
    fun compare(other: ComparisonVector): Int {
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
}
