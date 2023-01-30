package entity

import SymbolTable
import SystemFatalError
import expr.Notation
import expr.PointNotation

class PointRelations : EntityRelations() {
    val unknown = mutableSetOf<String>()
    override fun merge(other: Notation, symbolTable: SymbolTable) = throw SystemFatalError("Use mergePoints instead")

    fun mergePoints(self: PointNotation, other: PointNotation, symbolTable: SymbolTable) {
        unknown.addAll(symbolTable.getPoint(other).unknown)
        symbolTable.resetPoint(this, other)
        symbolTable.equalIdentRenamer.renameSubscribersAndPointer(other.p, self.p, symbolTable)
    }
}
