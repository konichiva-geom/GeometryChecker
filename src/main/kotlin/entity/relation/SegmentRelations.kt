package entity.relation

import entity.expr.notation.Notation
import entity.expr.notation.SegmentNotation
import pipeline.SymbolTable

class SegmentRelations : LinearRelations() {
    override fun merge(other: Notation?, symbolTable: SymbolTable, otherRelations: EntityRelations?) {
        mergeDifferentPoints((otherRelations ?: symbolTable.getSegment(other as SegmentNotation)) as SegmentRelations)
        if (other != null)
            symbolTable.resetSegment(this, other as SegmentNotation)
    }
}
