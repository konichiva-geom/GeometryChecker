package entity.expr.notation

import SymbolTable
import pipeline.interpreter.IdentMapper

class RayNotation(p1: String, p2: String) : Point2Notation(p1, p2) {
    init {
        this.p1 = p1
        this.p2 = p2
    }

    override fun mergeMapping(mapper: IdentMapper, other: Notation) {
        other as RayNotation
        mapper.mergeMapping(p1, listOf(other.p1))
        mapper.mergeMapping(p2, listOf(other.p2))
    }

    override fun createLinks(mapper: IdentMapper) {}

    override fun getOrder(): Int = 3

    override fun toLine() = Point2Notation(p1, p2)
    override fun mapIdents(mapper: IdentMapper) = RayNotation(mapper.get(p1), mapper.get(p2))

    override fun getRepr() = StringBuilder("ray AA")
    override fun toString(): String = "ray ${super.toString()}"

    override fun renameAndRemap(symbolTable: SymbolTable) {
        p1 = symbolTable.equalIdentRenamer.getIdentical(p1)
        p2 = symbolTable.equalIdentRenamer.getIdentical(p2)
    }
}
