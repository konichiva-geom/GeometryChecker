package entity

import SymbolTable
import expr.IdentNotation
import expr.Notation

class CircleRelations : EntityRelations() {
    val points = mutableSetOf<String>()
    override fun merge(other: Notation, symbolTable: SymbolTable) {
        points.addAll(symbolTable.getCircle(other as IdentNotation).points)
    }
}
