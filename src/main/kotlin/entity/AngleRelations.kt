package entity

import SymbolTable
import expr.Notation
import expr.Point3Notation

class AngleRelations : EntityRelations() {
    override fun merge(other: Notation, symbolTable: SymbolTable) {
        symbolTable.resetAngle(this, other as Point3Notation)
    }
}