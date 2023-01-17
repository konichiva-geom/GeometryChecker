package entity

import SymbolTable
import expr.Notation
import expr.PointNotation

class RayRelations : LineRelations()

class SegmentRelations : LineRelations()

class PointRelations : EntityRelations() {
    val unknown = mutableSetOf<String>()
    override fun merge(other: Notation, symbolTable: SymbolTable) {
        unknown.addAll(symbolTable.getPoint(other as PointNotation).unknown)
        symbolTable.resetPoint(this, other)
    }
}