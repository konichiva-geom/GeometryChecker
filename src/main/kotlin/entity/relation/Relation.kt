package entity.relation

import entity.expr.Expr
import pipeline.SymbolTable

/*
Creating and checking the existence of a relation. For expr
 */
interface Relation {
    fun check(symbolTable: SymbolTable): Boolean
    fun make(symbolTable: SymbolTable)

    companion object {
        fun makeRelation(relation: Relation, symbolTable: SymbolTable) {
            relation.make(symbolTable)
            symbolTable.inferenceProcessor.processInference(relation as Expr, symbolTable)
        }
    }
}
