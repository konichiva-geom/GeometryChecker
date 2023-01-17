package pipeline.interpreter

import SpoofError
import expr.Expr
import expr.Notation

/**
 * TODO move mapping from Theorem parser to this class to use in  [inference.Inference.process] too
 */
class ExpressionMapper {
    val mappings = mutableMapOf<String, MutableList<String>>()

    /**
     * On inference there might be ambiguous mappings
     * This method chooses first mapping and makes all mappings unique
     *
     */
    fun forceUniqueMappings() {
        for (key in mappings.keys) {
            if (mappings[key]!!.size != 1) {
                mappings[key] = mutableListOf(mappings[key]!!.first())
                removeFromOthers(key, mappings[key]!!.first())
            }
        }
    }

    fun mergeMapping(key: String, value: List<String>) {
        if (mappings[key] == null)
            mappings[key] = value.toMutableList()
        else {
            val res = mappings[key]!!.intersect(value.toSet())
            if (res.isEmpty())
                throw SpoofError(
                    "Got empty intersection while resolving theorem " +
                        "%{signature}. %{letter} maps to nothing.\n\tMappings: %{mappings}",
                    "letter" to key
                )
            mappings[key] = res.toMutableList()
            // if one mapping is unique, then it is removed from all other mappings
            if (res.size == 1)
                removeFromOthers(key, res.first())
        }
    }

    private fun removeFromOthers(key: String, removed: String) {
        for (otherKey in mappings.keys.filter { it != key })
            mappings[otherKey]!!.remove(removed)
    }

    fun clearMappings() {
        mappings.clear()
    }

    /**
     * Visit tree of args and build mappings
     */
    fun traverseExpr(call: Expr, definition: Expr) {
        if (call::class != definition::class)
            throw Exception("Expected ${definition::class}, got ${call::class}")
        if (call is Notation) {
            val callLetters = call.getLetters()
            val defLetters = (definition as Notation).getLetters()
            when (defLetters.size) {
                1 -> mergeMapping(defLetters.first(), callLetters)
                2 -> {
                    mergeMapping(defLetters.first(), callLetters)
                    mergeMapping(defLetters.last(), callLetters)
                }

                3 -> {
                    mergeMapping(defLetters[0], mutableListOf(callLetters.first(), callLetters.last()))
                    mergeMapping(defLetters[2], mutableListOf(callLetters.first(), callLetters.last()))
                    mergeMapping(defLetters[1], mutableListOf(callLetters[1]))
                }
            }
        }

        val (callChildren, defChildren) = listOf(call.getChildren(), definition.getChildren())
        if (callChildren.size != defChildren.size)
            throw Exception("Expected ${defChildren.size}, got ${callChildren.size}")
        for ((i, child) in callChildren.withIndex())
            traverseExpr(child, defChildren[i])
    }
}