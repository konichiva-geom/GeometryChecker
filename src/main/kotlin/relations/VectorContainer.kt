package relations

class VectorContainer<T> {
    val vectors = mutableMapOf<T, Vector>()


    /**
     * Substitute [nullified] index in all vectors
     */
    fun simplifyVectorCollection(nullified: Int, substitution: Vector) {
    }
}