package entity

import SymbolTable
import expr.Notation

//class entity.Angle(left:entity.Point, middle: entity.Point, right: entity.Point): Term
class AngleRelations : EntityRelations() {
    override fun isIn(other: Notation): Boolean {
        TODO("Not yet implemented")
    }

    override fun intersects(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isPerpendicular(): Boolean {
        TODO("Not yet implemented")
    }

    override fun merge(other: Notation, symbolTable: SymbolTable) {
        TODO("Not yet implemented")
    }
}