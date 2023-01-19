package pipeline.interpreter

import SpoofError
import Utils.addToOrCreateSet
import expr.Expr
import expr.Notation

/**
 * TODO move mapping from Theorem parser to this class to use in  [inference.Inference.process] too
 */
class ExpressionMapper {
    val mappings = mutableMapOf<String, MutableSet<String>>()
    val links = mutableMapOf<String, MutableSet<String>>()

    fun get(pointOrIdent: String) = mappings[pointOrIdent]!!.first()

    /**
     * On inference there might be ambiguous mappings
     * This method chooses first mapping and makes all mappings unique
     *
     */
    fun forceUniqueMappings() {
        for (key in mappings.keys) {
            if (mappings[key]!!.size != 1) {
                mappings[key] = mutableSetOf(mappings[key]!!.first())
                removeFromLinks(key, mappings[key]!!.first())
            }
        }
    }

    fun mergeMapping(key: String, value: List<String>) {
        if (mappings[key] == null)
            mappings[key] = value.toMutableSet()
        else {
            val res = mappings[key]!!.intersect(value.toSet())
            if (res.isEmpty())
                throw SpoofError(
                    "Got empty intersection while resolving theorem " +
                            "%{signature}. %{letter} maps to nothing.\n\tMappings: %{mappings}",
                    "letter" to key
                )
            mappings[key] = res.toMutableSet()
            // if one mapping is unique, then it is removed from all other mappings
            if (res.size == 1)
                removeFromLinks(key, res.first())
        }
    }

    private fun removeFromLinks(key: String, removed: String) {
        for (linked in links[key]!!) {
            mappings[linked]!!.remove(removed)
        }
    }

    fun addLink(first: String, second: String) {
        links.addToOrCreateSet(first, second)
        links.addToOrCreateSet(second, first)
    }

    fun createLinks(call: Expr, definition: Expr) {
        if (call::class != definition::class)
            throw SpoofError("Expected ${definition::class}, got ${call::class}")
        if (definition is Notation) {
            definition.createLinks(this)
        }
        val (callChildren, defChildren) = listOf(call.getChildren(), definition.getChildren())
        if (callChildren.size != defChildren.size)
            throw Exception("Expected ${defChildren.size}, got ${callChildren.size}")
        for ((i, child) in callChildren.withIndex())
            traverseExpr(child, defChildren[i])
    }

    fun clear() {
        links.clear()
        mappings.clear()
    }

    /**
     * Visit tree of args and build mappings
     */
    fun traverseExpr(call: Expr, definition: Expr) {
        if (call::class != definition::class)
            throw SpoofError("Expected ${definition::class}, got ${call::class}")
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