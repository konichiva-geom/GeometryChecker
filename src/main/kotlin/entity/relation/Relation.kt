package entity.relation

import SymbolTable

/*
Creating and checking the existence of a relation. For expr
 */
interface Relation {
    fun check(symbolTable: SymbolTable): Boolean
    fun make(symbolTable: SymbolTable)
}
