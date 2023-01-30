package relations

import Utils.PrimeGetter.primes
import Utils.addOrCreate

class VectorContainer<T> {
    val vectors = mutableMapOf<T, Vector>()
    private var currentIndex = 0

    fun getNext() = primes[currentIndex++]
    fun getCurrent() = primes[currentIndex]

    fun removeLast() {
        currentIndex--
    }

    fun getOrCreate(key: T): Vector {
        if (vectors[key] != null)
            return vectors[key]!!
        val res = Vector(getNext())
        vectors[key] = res
        return res
    }

    /**
     * Substitute [nullified] index in all vectors
     */
    fun simplifyVectorCollection(nullified: Int, substitution: Vector) {
        for (vector in vectors.values) {
            val coeff = vector[nullified] ?: continue
            vector.remove(nullified)
            substitution.multiplyBy(coeff)
            for ((key, element) in substitution) {
                vector.addOrCreate(key, element)
            }
        }
        // if nullified is not maxCurrentIndex, swap it with maxCurrentIndex
        if (nullified != getCurrent()) {
            for (vector in vectors.values) {
                if (vector[getCurrent()] != null)
                    vector[nullified] = vector[getCurrent()]!!
                vector.remove(getCurrent())
            }
            removeLast()
        }
    }
}