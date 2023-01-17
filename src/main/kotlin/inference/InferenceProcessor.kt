package inference

import SymbolTable
import expr.Expr
import pipeline.interpreter.ExpressionMapper

class InferenceProcessor {
    private val inferenceSets = mutableMapOf<String, MutableSet<Inference>>()
    private val mapper = ExpressionMapper()

    /**
     * Check all inferences for this expression
     */
    fun processInference(expr: Expr, symbolTable: SymbolTable) {
        val repr = expr.getRepr().toString()
        val inferences = inferenceSets[repr] ?: return
        for (inference in inferences) {
            inference.process(expr, symbolTable, mapper)
        }
    }

    /**
     * Form [inferenceSets]
     */
    fun setInference(list: List<Inference>) {
        inferenceSets.clear()
        for (inference in list) {
            addInferenceFromRepr(inference, inference.fromSideExpressions)
            if (inference is DoubleSidedInference)
                addInferenceFromRepr(inference, inference.toSideExpressions)
        }
    }

    private fun addInferenceFromRepr(inference: Inference, expressions: List<Expr>) {
        for (expr in expressions) {
            val repr = expr.getRepr().toString()
            if (inferenceSets[repr] == null)
                inferenceSets[repr] = mutableSetOf(inference)
            else
                inferenceSets[repr]!!.add(inference)
        }
    }
}
