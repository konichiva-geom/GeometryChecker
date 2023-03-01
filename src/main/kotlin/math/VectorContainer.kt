package math

import utils.ExtensionUtils.addOrCreate
import utils.PrimeGetter.primes


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

    fun resolveVector(v: Vector) {
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
            val divCoeff = v[setOf(nullified)]!!
            v.remove(setOf(nullified)) // TODO: divide all other vector values by nullified value here
            v.forEach { (_, u) -> u.unaryMinus().inPlaceDiv(divCoeff) }
            simplifyVectorCollection(nullified, v)
        }
    }

    /**
     * Substitute [nullified] index in all vectors
     */
    private fun simplifyVectorCollection(nullified: Int, substitution: Vector) {
        for (vector in vectors.values) {
            val coeff = vector[setOf(nullified)] ?: continue
            vector.remove(setOf(nullified))
            val multiplied = substitution.copy().multiplyBy(coeff)
            for ((key, element) in multiplied) {
                vector.addOrCreate(key, element)
            }
        }
        // if nullified is not maxCurrentIndex, swap it with maxCurrentIndex
        if (nullified != getCurrent()) {
            for (vector in vectors.values) {
                if (vector[setOf(getCurrent())] != null)
                    vector[setOf(nullified)] = vector[setOf(getCurrent())]!!
                vector.remove(setOf(getCurrent()))
            }
        }
        removeLast()
    }

    override fun toString(): String {
        return vectors.entries.joinToString { it.key.toString() + "->" + it.value.asString() }
    }
}
