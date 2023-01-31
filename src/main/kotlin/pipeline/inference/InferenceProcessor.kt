package pipeline.inference

import SymbolTable
import expr.Expr
import pipeline.interpreter.IdentMapper

class InferenceProcessor {
    private val inferenceSets = mutableMapOf<String, MutableSet<Inference>>()
    private val doubleInferenceSets = mutableMapOf<String, MutableSet<Pair<Inference, Boolean>>>()
    private val mapper = IdentMapper()

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
            addInferenceFromRepr(inference.fromSideExpressions, inferenceSets, inference)
            if (inference is DoubleSidedInference) {
                addInferenceFromRepr(inference.toSideExpressions, doubleInferenceSets, Pair(inference, true))
                addInferenceFromRepr(inference.fromSideExpressions, doubleInferenceSets, Pair(inference, false))
            }
        }
    }

    private fun <T> addInferenceFromRepr(
        expressions: List<Expr>,
        sets: MutableMap<String, MutableSet<T>>,
        added: T
    ) {
        for (expr in expressions) {
            val repr = expr.getRepr().toString()
            if (sets[repr] == null)
                sets[repr] = mutableSetOf(added)
            else
                sets[repr]!!.add(added)
        }
    }
}
