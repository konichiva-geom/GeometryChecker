package entity.relation

import entity.expr.notation.Notation
import entity.expr.notation.PointNotation
import error.SystemFatalError
import pipeline.SymbolTable

class PointRelations : EntityRelations() {
    val unknown = mutableSetOf<String>()
    override fun merge(other: Notation?, symbolTable: SymbolTable, otherRelations: EntityRelations?) = throw SystemFatalError("Use mergePoints instead")

    fun mergeOtherToThisPoint(self: PointNotation, other: PointNotation, symbolTable: SymbolTable) {
        unknown.addAll(symbolTable.getPoint(other).unknown)
        symbolTable.resetPoint(this, other)
        symbolTable.equalIdentRenamer.renameSubscribersAndPointer(other.p, self.p, symbolTable)
    }
}
