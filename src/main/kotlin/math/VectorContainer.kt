package math

import utils.ExtensionUtils.addOrCreate
import utils.Utils.primes
import utils.multiSetOf


class VectorContainer<T> {
    val vectors = mutableMapOf<T, Vector>()
    val incompleteVectors = mutableSetOf<Vector>()
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
        return mutableMapOf(res.keys.first() to res.values.first().copyOf())
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

        if (singleKeys.isEmpty())
            incompleteVectors.add(v)
        else {
            val nullified = singleKeys.first()
            val divCoeff = v[multiSetOf(nullified)]!!
            v.remove(multiSetOf(nullified))
            v.forEach { (_, u) -> u.inPlaceUnaryMinus().inPlaceDiv(divCoeff) }
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
}
