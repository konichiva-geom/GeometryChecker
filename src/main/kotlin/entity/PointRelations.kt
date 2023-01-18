package entity

import SymbolTable
import expr.Notation
import expr.PointNotation
import expr.RayNotation
import expr.SegmentNotation

class RayRelations : LinearRelations() {
    override fun merge(other: Notation, symbolTable: SymbolTable) {
        mergeDifferentPoints(symbolTable.getRay(other as RayNotation))
        symbolTable.resetRay(this, other)
    }
}

class SegmentRelations : LinearRelations() {
    override fun merge(other: Notation, symbolTable: SymbolTable) {
        mergeDifferentPoints(symbolTable.getSegment(other as SegmentNotation))
        symbolTable.resetSegment(this, other)
    }
}

class PointRelations : EntityRelations() {
    val unknown = mutableSetOf<String>()
    override fun merge(other: Notation, symbolTable: SymbolTable) {
        unknown.addAll(symbolTable.getPoint(other as PointNotation).unknown)
        symbolTable.resetPoint(this, other)
    }
}
