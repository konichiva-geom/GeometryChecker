package entity.relation

import entity.expr.notation.Notation
import entity.expr.notation.Point3Notation
import pipeline.symbol_table.SymbolTable

class AngleRelations : EntityRelations() {
    override fun merge(other: Notation?, symbolTable: SymbolTable, otherRelations: EntityRelations?) {
        if (other != null)
            symbolTable.resetAngle(this, other as Point3Notation)
        else {
            assert(symbolTable.angles.find { it.e2 == otherRelations } == null)
        }
    }
}