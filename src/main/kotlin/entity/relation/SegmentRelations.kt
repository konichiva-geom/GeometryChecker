package entity.relation

import SymbolTable
import entity.expr.notation.Notation
import entity.expr.notation.SegmentNotation

class SegmentRelations : LinearRelations() {
    override fun merge(other: Notation, symbolTable: SymbolTable) {
        mergeDifferentPoints(symbolTable.getSegment(other as SegmentNotation))
        symbolTable.resetSegment(this, other)
    }
}
