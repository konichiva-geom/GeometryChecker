package entity

import SymbolTable
import expr.Notation

class RayRelations : LineRelations()

class SegmentRelations : LineRelations()

class PointRelations : EntityRelations() {
    override fun merge(other: Notation, symbolTable: SymbolTable) {
        TODO("Not yet implemented")
    }
}