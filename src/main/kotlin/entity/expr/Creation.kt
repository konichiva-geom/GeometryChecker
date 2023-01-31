package entity.expr

import pipeline.SymbolTable

/**
 * Interface for creation (points, circles)
 */
interface Creation {
    fun create(symbolTable: SymbolTable)
}
