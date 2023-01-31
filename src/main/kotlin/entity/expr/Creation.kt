package entity.expr

import SymbolTable

/**
 * Interface for creation (points, circles)
 */
interface Creation {
    fun create(symbolTable: SymbolTable)
}
