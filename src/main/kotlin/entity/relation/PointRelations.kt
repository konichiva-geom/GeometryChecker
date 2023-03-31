package entity.relation

import entity.expr.notation.Notation
import error.SystemFatalError
import pipeline.symbol_table.SymbolTable

class PointRelations(val unknown: MutableSet<String> = mutableSetOf()) : EntityRelations() {
    override fun merge(other: Notation?, symbolTable: SymbolTable, otherRelations: EntityRelations?) =
        throw SystemFatalError("Use mergePoints instead")

    fun mergeOtherToThisPoint(self: String, other: String, symbolTable: SymbolTable) {
        unknown.addAll(symbolTable.getPoint(other).unknown - self)
        symbolTable.resetPoint(this, other)
        symbolTable.equalIdentRenamer.renameSubscribersAndPointer(other, self, symbolTable)
    }
}
