package math

import error.SpoofError
import external.WarnLogger
import utils.ExtensionUtils.addOrCreate
import utils.CommonUtils.primes
import utils.multiSetOf


class VectorContainer<T> {
    val vectors = mutableMapOf<T, Vector>()
    val incompleteVectors = mutableSetOf<Vector>()
    val notEqualVectors = mutableSetOf<Vector>()
    val biggerVectors = mutableListOf<Vector>()
    val biggerOrEqualVectors = mutableListOf<Vector>()
    private var currentIndex = 0

    fun getNext() = primes[currentIndex++]
    fun getCurrent() = primes[currentIndex - 1]

    fun removeLast() {
        currentIndex--
    }

    fun getOrCreate(key: T): Vector {
        if (vectors[key] != null)
            return vectors[key]!!.copy()
        val res = fromInt(getNext())
        vectors[key] = res
        return mutableMapOf(res.keys.first() to res.values.first())
    }

    fun resolveVector(v: Vector): Pair<Int, Int>? {
        val primeKeys = mutableSetOf<Int>()
        val multipliedKeys = mutableSetOf<Int>()
        for (key in v.keys) {
            if (key.size == 1)
                primeKeys.add(key.first())
            else
                multipliedKeys.addAll(key)
        }
        primeKeys.remove(0)

        val singleKeys = primeKeys - multipliedKeys

        if (singleKeys.isEmpty()) {
            if (multipliedKeys.size == 1) {

            } else {
                incompleteVectors.add(v)
            }
        } else {
            val nullified = singleKeys.first()
            val divCoeff = v[multiSetOf(nullified)]!!
            v.remove(multiSetOf(nullified))
            v.forEach { (k, u) -> v[k] = -u / divCoeff }
            return simplifyVectorCollection(nullified, v)
        }
        return null
    }

    /**
     * Substitute [nullified] index in all vectors
     */
    private fun simplifyVectorCollection(nullified: Int, substitution: Vector): Pair<Int, Int>? {
        for (vector in vectors.values) {
            val coeff = vector[multiSetOf(nullified)] ?: continue
            vector.remove(multiSetOf(nullified))
            val multiplied = substitution.copy().multiplyBy(coeff)
            for ((key, element) in multiplied) {
                vector.addOrCreate(key, element)
            }
        }
        // if nullified is not maxCurrentIndex, swap it with maxCurrentIndex
        val res = if (nullified != getCurrent()) getCurrent() to nullified else null
        if (nullified != getCurrent()) {
            for (vector in vectors.values) {
                if (vector[multiSetOf(getCurrent())] != null)
                    vector[multiSetOf(nullified)] = vector[multiSetOf(getCurrent())]!!
                vector.remove(multiSetOf(getCurrent()))
            }
        }
        removeLast()
        return res
    }

    override fun toString(): String {
        return vectors.entries.joinToString { it.key.toString() + "->" + it.value.asString() }
    }

    fun addNotEqualVector(value: Vector) {
        if (!notEqualVectors.contains(value)) {
            notEqualVectors.add(value)
        }
    }

    fun isBiggerContained(value: Vector): Boolean {
        for (vec in biggerVectors)
            when (value.compareWith(vec)) {
                0, 1 -> return true
                null, -1 -> continue
            }
        for (vec in biggerOrEqualVectors)
            when (value.compareWith(vec)) {
                0, -1, null -> continue
                1 -> return true
            }
        return false
    }

    fun isBiggerOrEqualContained(value: Vector): Boolean {
        for (vec in biggerVectors)
            when (value.compareWith(vec)) {
                0, 1 -> return true
                null, -1 -> continue
            }
        for (vec in biggerOrEqualVectors)
            when (value.compareWith(vec)) {
                -1, null -> continue
                0, 1 -> return true
            }
        return false
    }

    fun addBiggerVector(value: Vector) {
        var removed: Vector? = null
        for (vec in biggerVectors)
            when (value.compareWith(vec)) {
                null -> continue
                -1 -> {
                    removed = vec
                    break
                }

                0, 1 -> return
            }
        var removedFromBiggerOrEqual: Vector? = null
        for (vec in biggerOrEqualVectors) {
            when (value.compareWith(vec)) {
                0, null -> continue
                1 -> return
                -1 -> {
                    removedFromBiggerOrEqual = vec
                    break
                }
            }
        }
        if (removed != null) {
            biggerVectors.remove(removed)
        }
        if (removedFromBiggerOrEqual != null) {
            biggerOrEqualVectors.remove(removedFromBiggerOrEqual)
        }
        biggerVectors.add(value)
    }

    fun addBiggerOrEqualVector(value: Vector) {
        for (vec in biggerVectors)
            when (value.compareWith(vec)) {
                null -> continue
                0, 1 -> return
            }
        var removedFromBiggerOrEqual: Vector? = null
        for (vec in biggerOrEqualVectors) {
            when (value.compareWith(vec)) {
                0, 1 -> return
                null -> continue
                -1 -> {
                    removedFromBiggerOrEqual = vec
                    break
                }
            }
        }
        if (removedFromBiggerOrEqual != null) {
            biggerOrEqualVectors.remove(removedFromBiggerOrEqual)
        }
        biggerOrEqualVectors.add(value)
    }
}
