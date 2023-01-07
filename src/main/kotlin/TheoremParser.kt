import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.AlternativesFailure
import com.github.h0tk3y.betterParse.parser.ParseException
import expr.Expr
import expr.Notation
import pipeline.GeomGrammar
import pipeline.Parser

data class TheoremBody(val body: List<Expr>, val ret: List<Expr>) {
    override fun toString(): String {
        return "$ret"
    }
}

data class Signature(val name: String, val args: List<Expr>) {
    override fun toString(): String {
        return "$name(${args.joinToString(separator = ", ")})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Signature
        if (name != other.name) return false
        if (args.size != other.args.size) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + args.size.hashCode()
        return result
    }
}

class TheoremParser: Parser() {
    private val theorems = mutableMapOf<Signature, TheoremBody>()
    private val mappings = mutableMapOf<String, MutableList<String>>()
    fun clearTheorems() {
        theorems.clear()
    }

    private fun clearMappings() {
        mappings.clear()
    }

    fun addTheorems(theoremsCode: String) {
        try {
            theorems.putAll((GeomGrammar.parseToEnd(theoremsCode) as List<Pair<Signature, TheoremBody>>).toMap())
        } catch(e: ParseException) {
            val tokens = getAllErrorTokens(e.errorResult as AlternativesFailure)
            chooseFurthestUnexpectedToken(tokens)
            throw findProblemToken(e.errorResult as AlternativesFailure)
        }
    }

    fun getTheoremBodyBySignature(signature: Signature): TheoremBody {
        return theorems[signature] ?: throw SpoofError("Theorem %{name} not found", "name" to signature.name)
        /* TODO if theorem not found, make search by distance and suggest other variants:
        Theorem merge_projedvions not found, maybe you meant merge_projections(...)?
        */
    }

    fun getSignature(call: Signature): Signature {
        return theorems.keys.find { it.hashCode() == call.hashCode() } ?: throw Exception("Signature not found")
    }

    fun parseTheorem(call: Signature, theoremSignature: Signature, theoremBody: TheoremBody) {
        traverseSignature(call, theoremSignature)
        println(mappings)
        for (statement in theoremBody.body) {
            println()
        }
        clearMappings()
    }

    fun check(relation: Relation, symbolTable: SymbolTable) {
        if(!relation.check(symbolTable))
            throw SpoofError("Relation unknown")
    }

    fun traverseSignature(callSignature: Signature, defSignature: Signature) {
        for ((i, arg) in callSignature.args.withIndex())
            traverseExpr(arg, defSignature.args[i])
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
                for (otherKey in mappings.keys.filter { it != key })
                    mappings[otherKey]!!.remove(res.first())
        }
    }
}
