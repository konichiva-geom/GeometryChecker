package entity.relation

import entity.expr.notation.IdentNotation
import entity.expr.notation.Notation
import pipeline.SymbolTable

class CircleRelations : EntityRelations() {
    val points = mutableSetOf<String>()
    override fun merge(other: Notation, symbolTable: SymbolTable) {
        points.addAll(symbolTable.getCircle(other as IdentNotation).points)
    }
}
