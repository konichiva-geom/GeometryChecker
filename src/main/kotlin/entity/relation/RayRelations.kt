package entity.relation

import entity.expr.notation.Notation
import entity.expr.notation.RayNotation
import pipeline.symbol_table.SymbolTable

class RayRelations : LinearRelations() {
    override val pointsNotContained: MutableSet<String> = mutableSetOf()
    override fun merge(other: Notation?, symbolTable: SymbolTable, otherRelations: EntityRelations?) {
        mergeDifferentPoints((otherRelations ?: symbolTable.getRay(other as RayNotation)) as RayRelations)
        if (other != null)
            symbolTable.resetRay(this, other as RayNotation)
    }
}
