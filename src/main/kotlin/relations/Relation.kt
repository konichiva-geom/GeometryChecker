package relations

import SymbolTable

/*
Creating and checking the existence of a relation
 */
interface Relation {
    fun check(symbolTable: SymbolTable): Boolean
    fun make(symbolTable: SymbolTable)
}
