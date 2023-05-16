package entity.relation

import entity.expr.notation.ArcNotation
import entity.expr.notation.Notation
import entity.expr.notation.SegmentNotation
import pipeline.symbol_table.SymbolTable

class ArcRelations : LinearRelations() {
    override val pointsNotContained: MutableSet<String> = mutableSetOf()
    val points = mutableSetOf<String>()
    override fun merge(other: Notation?, symbolTable: SymbolTable, otherRelations: EntityRelations?) {
        mergeDifferentPoints((otherRelations ?: symbolTable.getArc(other as ArcNotation)) as ArcRelations)
        points.addAll(symbolTable.getArc(other as ArcNotation).points)
    }
}