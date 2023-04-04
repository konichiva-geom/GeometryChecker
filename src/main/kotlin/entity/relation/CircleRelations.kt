package entity.relation

import entity.Renamable
import entity.expr.notation.IdentNotation
import entity.expr.notation.Notation
import error.SystemFatalError
import pipeline.symbol_table.SymbolTable

class CircleRelations : EntityRelations(), Renamable {
    val unknown = mutableSetOf<String>()
    val points = mutableSetOf<String>()

    override fun merge(other: Notation?, symbolTable: SymbolTable, otherRelations: EntityRelations?) {
        throw SystemFatalError("use ")
    }

    fun mergeOtherToThisCircle(self: IdentNotation, other: IdentNotation, symbolTable: SymbolTable) {
        points.addAll(symbolTable.getCircle(other).points)
        unknown.addAll(symbolTable.getCircle(other).unknown)
        symbolTable.resetCircle(this, other)
        symbolTable.equalIdentRenamer.renameSubscribersAndPointer(other.text, self.text, symbolTable)
    }

    override fun renameToMinimalAndRemap(symbolTable: SymbolTable) {
        val renamed = points.map { symbolTable.equalIdentRenamer.getIdentical(it) }
        points.clear()
        points.addAll(renamed)
    }

    override fun checkValidityAfterRename(): Exception? {
        return null
    }

    fun addPoints(symbolTable: SymbolTable, vararg addedPoints: String) {
        symbolTable.equalIdentRenamer.removeSubscribers(this, *points.toTypedArray())
        points.addAll(addedPoints)
        symbolTable.equalIdentRenamer.addSubscribers(this, *points.toTypedArray())
    }

    fun getCirclePoints() = points.toSet()
}
