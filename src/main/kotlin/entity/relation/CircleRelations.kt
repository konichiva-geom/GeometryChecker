package entity.relation

import SymbolTable
import entity.expr.notation.IdentNotation
import entity.expr.notation.Notation

class CircleRelations : EntityRelations() {
    val points = mutableSetOf<String>()
    override fun merge(other: Notation, symbolTable: SymbolTable) {
        points.addAll(symbolTable.getCircle(other as IdentNotation).points)
    }
}
