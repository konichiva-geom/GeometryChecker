package entity

import SymbolTable
import expr.ArcNotation
import expr.Notation

class ArcRelations : LinearRelations() {
    val points = mutableSetOf<String>()
    override fun merge(other: Notation, symbolTable: SymbolTable) {
        points.addAll(symbolTable.getArc(other as ArcNotation).points)
    }
}