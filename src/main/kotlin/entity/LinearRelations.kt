package entity

import SymbolTable
import expr.Notation

abstract class LinearRelations : EntityRelations() {
    // points not in linear
    protected val differentPoints = mutableSetOf<String>()

    override fun merge(other: Notation, symbolTable: SymbolTable) {
        TODO("Not yet implemented")
    }

    protected fun mergeDifferentPoints(other: LinearRelations) {
        this.differentPoints.addAll(other.differentPoints)
        other.differentPoints.clear()
    }
}
