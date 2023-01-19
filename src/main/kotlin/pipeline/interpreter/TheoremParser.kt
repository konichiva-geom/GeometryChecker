package pipeline.interpreter

import Relation
import SpoofError
import SymbolTable
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.AlternativesFailure
import com.github.h0tk3y.betterParse.parser.ParseException
import expr.Expr
import expr.TheoremUse
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

class TheoremParser : Parser() {
    private val theorems = mutableMapOf<Signature, TheoremBody>()
    private val signatureMapper = ExpressionMapper()

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
        /* TODO if theorem not found, make search by distance and suggest other variants:
        Theorem merge_projedvions not found, maybe you meant merge_projections(...)?
        */
    }

    fun getSignature(call: Signature): Signature {
        return theorems.keys.find { it.hashCode() == call.hashCode() }
            ?: throw Exception("pipeline.interpreter.Signature not found")
    }

    fun parseTheorem(call: Signature, theoremSignature: Signature, theoremBody: TheoremBody, symbolTable: SymbolTable) {
        traverseSignature(call, theoremSignature)
        println(signatureMapper.mappings)
        // TODO map mappings to body
        for (expr in theoremBody.body) {
            when (expr) {
                is Relation -> (expr.rename(signatureMapper) as Relation).make(symbolTable)
                is TheoremUse -> {
                    if (expr.signature.name == "check")
                        check(expr.signature.args.map { it.rename(signatureMapper) }, symbolTable)
                    else throw SpoofError("Expected relation to check")
                }
            }
        }
        if (theoremBody.ret.isNotEmpty()) {
            for (expr in theoremBody.ret)
                (expr.rename(signatureMapper) as Relation).make(symbolTable)
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
            if (!rel.check(symbolTable))
                throw SpoofError("Relation ${rel} unknown")
        }
    }

    private fun traverseSignature(callSignature: Signature, defSignature: Signature) {
        for ((i, arg) in callSignature.args.withIndex())
            signatureMapper.createLinks(arg, defSignature.args[i])
        for ((i, arg) in callSignature.args.withIndex())
            signatureMapper.traverseExpr(arg, defSignature.args[i])
    }
}
