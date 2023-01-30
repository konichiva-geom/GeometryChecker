package entity

import SymbolTable
import expr.Notation
import expr.RayNotation

class RayRelations : LinearRelations() {
    override fun merge(other: Notation, symbolTable: SymbolTable) {
        mergeDifferentPoints(symbolTable.getRay(other as RayNotation))
        symbolTable.resetRay(this, other)
    }
}
