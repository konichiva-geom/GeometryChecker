package entity.relation

import entity.Renamable
import entity.expr.notation.IdentNotation
import entity.expr.notation.Notation
import pipeline.SymbolTable

class CircleRelations : EntityRelations(), Renamable {
    private val points = mutableSetOf<String>()
    override fun merge(other: Notation?, symbolTable: SymbolTable, otherRelations: EntityRelations?) {
        points.addAll(symbolTable.getCircle(other as IdentNotation).points)
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

    fun getPoints() = points.toSet()
}
