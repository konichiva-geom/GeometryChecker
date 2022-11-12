import entity.Entity

class VectorContainer<T : Entity> {
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

data class SegmentRelation(var intersects: Entity?) {}

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
        return ComparisonVector((value.keys + other.value.keys)
            .associateWith { operation(value[it] ?: 0f, other.value[it] ?: 0f) }
            .toMutableMap())
    }

    /**
     * Multiply or divide current vector by [other]
     */
    fun transform(other: Float, operation: (Float, Float) -> Float): ComparisonVector {
        return ComparisonVector(value.mapValues { operation(it.value, other) }.toMutableMap())
    }
}