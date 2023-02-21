package entity.relation

import entity.expr.notation.Notation
import entity.expr.notation.Point3Notation
import pipeline.SymbolTable

class AngleRelations : EntityRelations() {
    override fun merge(other: Notation?, symbolTable: SymbolTable, otherRelations: EntityRelations?) {
        symbolTable.resetAngle(this, other as Point3Notation)
    }
}