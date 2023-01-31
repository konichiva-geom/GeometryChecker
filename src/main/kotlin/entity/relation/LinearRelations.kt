package entity.relation

import entity.expr.notation.Notation
import pipeline.SymbolTable

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
