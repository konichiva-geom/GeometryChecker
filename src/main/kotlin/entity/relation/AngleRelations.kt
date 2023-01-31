package entity.relation

import SymbolTable
import entity.expr.notation.Notation
import entity.expr.notation.Point3Notation

class AngleRelations : EntityRelations() {
    override fun merge(other: Notation, symbolTable: SymbolTable) {
        symbolTable.resetAngle(this, other as Point3Notation)
    }
}