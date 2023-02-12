package entity.expr

import pipeline.SymbolTable

/**
 * Expression that returns some value, e.g. [BinaryIntersects] returns point, or segment, or something else
 */
interface Returnable {
    fun getReturnValue(symbolTable: SymbolTable): Set<String>
}
