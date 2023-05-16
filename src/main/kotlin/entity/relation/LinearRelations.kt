package entity.relation

import entity.expr.binary_expr.NotContainable
import entity.expr.notation.Notation
import error.SystemFatalError
import pipeline.symbol_table.SymbolTable

abstract class LinearRelations : EntityRelations(), NotContainable {

    /**
     * Delete [other], merge its relations with [this]
     */
    override fun merge(other: Notation?, symbolTable: SymbolTable, otherRelations: EntityRelations?) {
        throw SystemFatalError("Should be overridden in inheritors")
    }
}
