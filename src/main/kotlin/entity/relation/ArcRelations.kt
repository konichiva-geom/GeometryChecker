package entity.relation

import entity.expr.notation.ArcNotation
import entity.expr.notation.Notation
import pipeline.SymbolTable

class ArcRelations : LinearRelations() {
    val points = mutableSetOf<String>()
    override fun merge(other: Notation, symbolTable: SymbolTable) {
        points.addAll(symbolTable.getArc(other as ArcNotation).points)
    }
}