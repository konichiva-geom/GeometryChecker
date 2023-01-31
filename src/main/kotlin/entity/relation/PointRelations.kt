package entity.relation

import SymbolTable
import SystemFatalError
import entity.expr.notation.Notation
import entity.expr.notation.PointNotation

class PointRelations : EntityRelations() {
    val unknown = mutableSetOf<String>()
    override fun merge(other: Notation, symbolTable: SymbolTable) = throw SystemFatalError("Use mergePoints instead")

    fun mergePoints(self: PointNotation, other: PointNotation, symbolTable: SymbolTable) {
        unknown.addAll(symbolTable.getPoint(other).unknown)
        symbolTable.resetPoint(this, other)
        symbolTable.equalIdentRenamer.renameSubscribersAndPointer(other.p, self.p, symbolTable)
    }
}
