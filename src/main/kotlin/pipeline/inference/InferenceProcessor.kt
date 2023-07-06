package pipeline.inference

import entity.expr.Expr
import entity.expr.binary_expr.BinaryExpr
import entity.expr.notation.Notation
import entity.expr.notation.PointNotation
import math.ArithmeticExpr
import pipeline.symbol_table.SymbolTable
import pipeline.interpreter.IdentMapper

class InferenceProcessor {
    private val inferenceSets = mutableMapOf<String, MutableSet<Inference>>()
    private val doubleInferenceSets = mutableMapOf<String, MutableSet<Pair<Inference, Boolean>>>()
    private val mapper = IdentMapper()
    private val processedSet = mutableSetOf<String>()

    /**
     * Check all inferences for this expression
     */
    fun processInference(expr: Expr, symbolTable: SymbolTable) {
        val currentExpression = expr.toString()
        if (processedSet.contains(currentExpression))
            return
        val repr = expr.getRepr().toString()
        processedSet.add(currentExpression)
        val inferences = inferenceSets[repr] ?: return
        for (inference in inferences) {
            inference.process(expr, symbolTable, mapper, this)
        }
    }

    /**
     * Form [inferenceSets]
     */
    fun setInference(list: List<Inference>) {
        inferenceSets.clear()
        for (inference in list) {
            addInferenceFromRepr(inference.fromSideExpressions, inferenceSets, inference)
//            if (inference is DoubleSidedInference) {
//                addInferenceFromRepr(inference.toSideExpressions, doubleInferenceSets, Pair(inference, true))
//                addInferenceFromRepr(inference.fromSideExpressions, doubleInferenceSets, Pair(inference, false))
//            }
        }
    }

    private fun <T : Inference> addInferenceFromRepr(
        expressions: List<Expr>,
        sets: MutableMap<String, MutableSet<T>>,
        added: T
    ) {
        for (expr in expressions) {
            val repr = expr.getRepr().toString()
            val allPoints = expr.getAllPoints()
            // don't add expr to map if inference has `any` with point from it
            if (added.fromSideQuantifier.any { (it.notation as PointNotation).p in allPoints })
                continue
            if (sets[repr] == null)
                sets[repr] = mutableSetOf(added)
            else
                sets[repr]!!.add(added)
        }
    }

    fun clearAfterProcessingRelation() {
        processedSet.clear()
    }
}
