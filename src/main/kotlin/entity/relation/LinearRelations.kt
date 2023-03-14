package entity.relation

import entity.expr.notation.Notation
import error.SystemFatalError
import pipeline.symbol_table.SymbolTable

abstract class LinearRelations : EntityRelations() {
    // points not in linear
    protected val differentPoints = mutableSetOf<String>()

    /**
     * Delete [other], merge its relations with [this]
     */
    override fun merge(other: Notation?, symbolTable: SymbolTable, otherRelations: EntityRelations?) {
        throw SystemFatalError("Should be overridden in inheritors")
    }

    protected fun mergeDifferentPoints(other: LinearRelations) {
        this.differentPoints.addAll(other.differentPoints)
        other.differentPoints.clear()
    }
}
