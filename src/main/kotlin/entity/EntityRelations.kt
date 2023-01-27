package entity

import SymbolTable
import expr.ArcNotation
import expr.IdentNotation
import expr.Notation

abstract class EntityRelations // A, |>ABC, BC, AOB
{
    abstract fun merge(other: Notation, symbolTable: SymbolTable)
}

class CircleRelations : EntityRelations() {
    val points = mutableSetOf<String>()
    override fun merge(other: Notation, symbolTable: SymbolTable) {
        points.addAll(symbolTable.getCircle(other as IdentNotation).points)
    }
}

class ArcRelations : LinearRelations() {
    val points = mutableSetOf<String>()
    override fun merge(other: Notation, symbolTable: SymbolTable) {
        points.addAll(symbolTable.getArc(other as ArcNotation).points)
    }
}