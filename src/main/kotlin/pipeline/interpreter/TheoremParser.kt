package pipeline.interpreter

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.AlternativesFailure
import com.github.h0tk3y.betterParse.parser.ParseException
import entity.expr.Expr
import entity.expr.Invocation
import entity.expr.Relation
import error.SpoofError
import pipeline.SymbolTable
import pipeline.parser.GeomGrammar
import pipeline.parser.Parser

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

class TheoremParser : Parser() {
    private val theorems = mutableMapOf<Signature, TheoremBody>()
    private val signatureMapper = IdentMapper()

    fun clearTheorems() {
        theorems.clear()
    }

    fun addTheorems(theoremsCode: String) {
        try {
            theorems.putAll((GeomGrammar.parseToEnd(theoremsCode) as List<Pair<Signature, TheoremBody>>).toMap())
        } catch (e: ParseException) {
            val tokens = getAllErrorTokens(e.errorResult as AlternativesFailure)
            chooseFurthestUnexpectedToken(tokens)
            throw findProblemToken(e.errorResult as AlternativesFailure)
        }
    }

    fun getTheoremBodyBySignature(signature: Signature): TheoremBody {
        return theorems[signature] ?: throw SpoofError("Theorem %{name} not found", "name" to signature.name)
    }

    fun getSignature(call: Signature): Signature {
        return theorems.keys.find { it.hashCode() == call.hashCode() }
            ?: throw Exception("signature not found")
    }

    fun parseTheorem(call: Signature, theoremSignature: Signature, theoremBody: TheoremBody, symbolTable: SymbolTable) {
        traverseSignature(call, theoremSignature)
        println(signatureMapper.mappings)
        for (expr in theoremBody.body) {
            when (expr) {
                is Relation -> Relation.makeRelation(
                    expr.createNewWithMappedPointsAndCircles(signatureMapper) as Relation,
                    symbolTable,
                    fromInference = false
                )
                is Invocation -> {
                    if (expr.signature.name == "check")
                        check(
                            expr.signature.args.map { it.createNewWithMappedPointsAndCircles(signatureMapper) },
                            symbolTable
                        )
                    else throw SpoofError("Expected relation to check")
                }
            }
        }
        if (theoremBody.ret.isNotEmpty()) {
            for (expr in theoremBody.ret)
                Relation.makeRelation(
                    expr.createNewWithMappedPointsAndCircles(signatureMapper) as Relation,
                    symbolTable,
                    fromInference = false
                )
        }
        signatureMapper.clear()
    }

    fun check(relation: Relation, symbolTable: SymbolTable) {
        if (!relation.check(symbolTable))
            throw SpoofError("Relation ${relation as Expr} unknown")
    }

    fun check(expressions: List<Expr>, symbolTable: SymbolTable) {
        for (rel in expressions) {
            if (rel !is Relation)
                throw SpoofError("Cannot check %{expr}, because it is not a relation", "expr" to rel)
            check(rel, symbolTable)
        }
    }

    private fun traverseSignature(callSignature: Signature, defSignature: Signature) {
        for ((i, arg) in callSignature.args.withIndex())
            signatureMapper.createLinks(arg, defSignature.args[i])
        for ((i, arg) in callSignature.args.withIndex())
            signatureMapper.traverseExpr(arg, defSignature.args[i])
    }
}
