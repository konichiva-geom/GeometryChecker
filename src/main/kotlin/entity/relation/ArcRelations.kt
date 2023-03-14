package entity.relation

import entity.expr.notation.ArcNotation
import entity.expr.notation.Notation
import pipeline.symbol_table.SymbolTable

class ArcRelations : LinearRelations() {
    val points = mutableSetOf<String>()
    override fun merge(other: Notation?, symbolTable: SymbolTable, otherRelations: EntityRelations?) {
        points.addAll(symbolTable.getArc(other as ArcNotation).points)
    }
}