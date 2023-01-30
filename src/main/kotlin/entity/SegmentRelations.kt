package entity

import SymbolTable
import expr.Notation
import expr.SegmentNotation

class SegmentRelations : LinearRelations() {
    override fun merge(other: Notation, symbolTable: SymbolTable) {
        mergeDifferentPoints(symbolTable.getSegment(other as SegmentNotation))
        symbolTable.resetSegment(this, other)
    }
}
