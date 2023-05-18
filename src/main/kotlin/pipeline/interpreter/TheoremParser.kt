package pipeline.interpreter

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.AlternativesFailure
import com.github.h0tk3y.betterParse.parser.ParseException
import com.github.h0tk3y.betterParse.parser.UnparsedRemainder
import entity.expr.Creation
import entity.expr.Expr
import entity.expr.Invocation
import entity.expr.Relation
import entity.expr.binary_expr.BinaryAssignment
import entity.expr.notation.Notation
import error.PosError
import error.SpoofError
import pipeline.inference.InferenceProcessor
import pipeline.parser.GeomGrammar
import pipeline.parser.Parser
import pipeline.symbol_table.SymbolTable
import utils.ExtensionUtils.toRange

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

    fun addTheorems(theorems: Map<Signature, TheoremBody>) {
        this.theorems.putAll(theorems)
    }

    fun addTheorems(theoremsCode: String) {
        try {
            theorems.putAll((GeomGrammar.parseToEnd(theoremsCode) as List<Pair<Signature, TheoremBody>>).toMap())
        } catch (e: ParseException) {
            if (e.errorResult is UnparsedRemainder) {
                throw PosError((e.errorResult as UnparsedRemainder).startsWith.toRange(), e.message!!)
            }
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
            ?: throw SpoofError("signature not found")
    }

    fun parseTheorem(
        call: Invocation,
        theoremSignature: Signature,
        theoremBody: TheoremBody,
        symbolTable: SymbolTable,
        inferenceProcessor: InferenceProcessor
    ) {
        checkSignature(call.signature, symbolTable, inferenceProcessor)
        traverseSignature(call.signature, theoremSignature)
        for (expr in theoremBody.body) {
            when (expr) {
                is BinaryAssignment -> expr.makeAssignment(symbolTable, inferenceProcessor)
                is Relation -> Relation.makeRelation(
                    expr.createNewWithMappedPointsAndCircles(signatureMapper) as Relation,
                    symbolTable,
                    inferenceProcessor,
                    fromInference = false
                )

                is Invocation -> {
                    if (expr.signature.name == "check") {
                        check(
                            expr.signature
                                .args.map { it.createNewWithMappedPointsAndCircles(signatureMapper) }, symbolTable
                        )
                    } else {
                        val mappedExpr = Invocation(
                            Signature(expr.signature.name,
                                expr.signature.args.map { it.createNewWithMappedPointsAndCircles(signatureMapper) }
                            ), expr.output.map { it.createNewWithMappedPointsAndCircles(signatureMapper) })
                        val mappings = signatureMapper.mappings.toMap()
                        signatureMapper.clear()
                        parseTheorem(
                            mappedExpr,
                            getSignature(expr.signature),
                            getTheoremBodyBySignature(expr.signature),
                            symbolTable,
                            inferenceProcessor
                        )
                        signatureMapper.mappings.putAll(mappings)
                    }
                    //else throw SpoofError("Expected relation to check")
                }

                is Creation -> throw SpoofError("Cannot create new points and circles inside theorem")
            }
        }
        if (theoremBody.ret.isNotEmpty()) {
            val returnedExpressions = getReturnedExpressions(call.output, theoremBody.ret)
            for (expr in returnedExpressions) {
                Relation.makeRelation(
                    expr as Relation,
                    symbolTable,
                    inferenceProcessor,
                    fromInference = false
                )
            }
        }
        signatureMapper.clear()
    }

    private fun getReturnedExpressions(callReturn: List<Expr>, defReturn: List<Expr>): List<Expr> {
        if (callReturn.isEmpty())
            return defReturn.map { it.createNewWithMappedPointsAndCircles(signatureMapper) }
        val res = mutableListOf<Expr>()
        val mappedDefReturn = defReturn.map { it.createNewWithMappedPointsAndCircles(signatureMapper).toString() }
        for (expr in callReturn) {
            if (mappedDefReturn.contains(expr.toString())) {
                res.add(expr)
            } else throw SpoofError("Expression $expr is not a return value of theorem")
        }
        return res
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
        signatureMapper.forceUniqueMappings()
    }

    /**
     * Check that all relations are correct and make construction relations
     */
    private fun checkSignature(
        callSignature: Signature,
        symbolTable: SymbolTable,
        inferenceProcessor: InferenceProcessor
    ) {
        for (expr in callSignature.args) {
            when (expr) {
                is Creation -> expr.create(symbolTable, inferenceProcessor)
                is Relation -> check(expr, symbolTable)
                is Notation -> {}
                else -> throw SpoofError("Expected relation or creation")
            }
        }
    }

    companion object {
        fun check(relation: Relation, symbolTable: SymbolTable) {
            if (!relation.check(symbolTable))
                throw SpoofError("Relation ${relation as Expr} unknown")
        }
    }
}
