package entity.expr

import pipeline.SymbolTable

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
        fun makeRelation(relation: Relation, symbolTable: SymbolTable, fromInference: Boolean = false) {
            if (!fromInference)
                symbolTable.inferenceProcessor.clearAfterProcessingRelation()
            relation.make(symbolTable)
            symbolTable.inferenceProcessor.processInference(relation as Expr, symbolTable)
        }
    }
}
