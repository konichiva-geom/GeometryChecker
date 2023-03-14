package entity.expr

import pipeline.symbol_table.SymbolTable

/**
 * Interface for creation (points, circles)
 */
interface Creation {
    fun create(symbolTable: SymbolTable)
}
