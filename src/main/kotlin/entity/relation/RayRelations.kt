package entity.relation

import SymbolTable
import entity.expr.notation.Notation
import entity.expr.notation.RayNotation

class RayRelations : LinearRelations() {
    override fun merge(other: Notation, symbolTable: SymbolTable) {
        mergeDifferentPoints(symbolTable.getRay(other as RayNotation))
        symbolTable.resetRay(this, other)
    }
}
