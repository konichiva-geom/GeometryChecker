package entity

import SymbolTable
import expr.Notation
import expr.Point2Notation
import relations.In
import symbolTable

class RayRelations : LineRelations() {

}

class SegmentRelations : LineRelations() {
}

class PointRelations : EntityRelations() {
    val unknown = mutableSetOf<String>()

    fun isIn(name: Point2Notation): Boolean {
        In.inMap[this]?.contains(symbolTable.getRay(name.toRayNotation()))
        return In.inMap[this]?.contains(symbolTable.getLine(name))!!
    }

    override fun merge(other: Notation, symbolTable: SymbolTable) {
        TODO("Not yet implemented")
    }
}