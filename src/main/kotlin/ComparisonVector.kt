import Utils.mergeWithOperation
import entity.EntityRelations

class VectorContainer<T : EntityRelations> {
    private val vectors = mutableMapOf<T, ComparisonVector>()
    private var maxDim = 0

    fun addNewVector(entity: T) {
        maxDim += 1
        vectors[entity] = ComparisonVector(mutableMapOf(maxDim to 1f)) // TODO `0 to 0f` is important?
    }
}

enum class ComparisonType {
    EQUALS,
    GREATER_THAN,
    GREATER_OR_EQUALS
}

data class SegmentRelation(var intersects: EntityRelations?)

class Vector<T>(
    private val value: MutableMap<T, Float>
) : MutableMap<T, Float> {
    override val size: Int = value.size
    override val entries: MutableSet<MutableMap.MutableEntry<T, Float>> = value.entries
    override val keys: MutableSet<T> = value.keys
    override val values: MutableCollection<Float> = value.values
    override fun containsKey(key: T): Boolean = value.containsKey(key)
    override fun containsValue(value: Float): Boolean = this.value.containsValue(value)
    override fun get(key: T): Float? = value[key]
    override fun clear() = value.clear()
    override fun isEmpty(): Boolean = value.isEmpty()
    override fun remove(key: T): Float? = value.remove(key)
    override fun putAll(from: Map<out T, Float>) = value.putAll(from)
    override fun put(key: T, value: Float): Float? = this.value.put(key, value)
}

class ComparisonVector(
    private val value: MutableMap<Int, Float>,
    private val type: ComparisonType = ComparisonType.EQUALS,
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

    /**
     * Merge current vectors by addition or subtraction
     */
    fun merge(other: ComparisonVector, operation: (Float, Float) -> Float): ComparisonVector {
        return ComparisonVector(value.mergeWithOperation(other.value, operation))
    }

    /**
     * Multiply or divide current vector by [other]
     */
    fun transform(other: Float, operation: (Float, Float) -> Float): ComparisonVector {
        return ComparisonVector(value.mapValues { operation(it.value, other) }.toMutableMap())
    }
}