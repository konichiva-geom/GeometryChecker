class VectorContainer() {
    val vectors = mutableMapOf<Entity, ComparisonVector>()
    var maxDim = 0

    fun getNewVector(): ComparisonVector {
        maxDim += 1
        return ComparisonVector(mutableMapOf(maxDim to 1f))
    }
}

class ComparisonVector(val value: MutableMap<Int, Float>) {
    /**
     * -1 for this smaller than other
     * 0 for this equal to other
     * 1 for this bigger than other
     * 2 for this incomparable than other
     */
    fun compare(other: ComparisonVector): Int {
        var res = mutableSetOf<Int>()
        for (i in value.keys) {
            if (i in other.value.keys) {
                res.add(if (value[i]!! == other.value[i]!!) 0 else)
            } else res.add(1)
        }
        return res
    }

    fun plus(other: ComparisonVector) {
        val res = ComparisonVector(mutableMapOf())
        for (i in value) {
        }
    }

    fun multiply(other: Int) {}
}