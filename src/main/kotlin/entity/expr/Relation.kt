package entity.expr

import pipeline.inference.InferenceProcessor
import pipeline.symbol_table.SymbolTable

/**
 * Creating and checking the existence of a relation. For entity.expr
 */
interface Relation {
    fun check(symbolTable: SymbolTable): Boolean

    /**
     * Use [makeRelation] instead
     */
    fun make(symbolTable: SymbolTable)

    companion object {
        fun makeRelation(
            relation: Relation,
            symbolTable: SymbolTable,
            inferenceProcessor: InferenceProcessor,
            fromInference: Boolean = false
        ) {
            if (!fromInference)
                inferenceProcessor.clearAfterProcessingRelation()
            relation.make(symbolTable)
            inferenceProcessor.processInference(relation as Expr, symbolTable)
        }
    }
}
