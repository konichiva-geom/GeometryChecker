package entity

import SymbolTable
import SystemFatalError
import expr.Notation
import expr.PointNotation
import expr.RayNotation
import expr.SegmentNotation

class RayRelations : LinearRelations() {
    override fun merge(other: Notation, symbolTable: SymbolTable) {
        mergeDifferentPoints(symbolTable.getRay(other as RayNotation))
        symbolTable.resetRay(this, other)
    }
}

class SegmentRelations : LinearRelations() {
    override fun merge(other: Notation, symbolTable: SymbolTable) {
        mergeDifferentPoints(symbolTable.getSegment(other as SegmentNotation))
        symbolTable.resetSegment(this, other)
    }
}

class PointRelations : EntityRelations() {
    val unknown = mutableSetOf<String>()
    override fun merge(other: Notation, symbolTable: SymbolTable) = throw SystemFatalError("Use mergePoints instead")

    fun mergePoints(self: PointNotation, other: PointNotation, symbolTable: SymbolTable) {
        unknown.addAll(symbolTable.getPoint(other).unknown)
        symbolTable.resetPoint(this, other)
        symbolTable.equalIdentRenamer.renameSubscribersAndPointer(other.p, self.p, symbolTable)
    }
}
