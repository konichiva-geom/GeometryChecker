package entity.relation

import entity.expr.notation.Notation
import error.SpoofError
import error.SystemFatalError
import pipeline.symbol_table.SymbolTable

class PointRelations(val unknown: MutableSet<String> = mutableSetOf()) : EntityRelations() {
    override fun merge(other: Notation?, symbolTable: SymbolTable, otherRelations: EntityRelations?) =
        throw SystemFatalError("Use mergePoints instead")

    fun mergeOtherToThisPoint(self: String, other: String, symbolTable: SymbolTable) {
        val otherPointRelations = symbolTable.getPoint(other)
        if (!otherPointRelations.unknown.contains(self) || !unknown.contains(other))
            throw SpoofError(
                "Distinct points %{first}, %{second} cannot be made equal", "first" to self, "second" to other
            )
        unknown.addAll(otherPointRelations.unknown - self)
        symbolTable.resetPoint(this, other)
        symbolTable.equalIdentRenamer.renameSubscribersAndPointer(other, self, symbolTable)
    }
}
