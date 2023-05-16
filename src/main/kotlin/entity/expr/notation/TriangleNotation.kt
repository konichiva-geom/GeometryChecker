package entity.expr.notation

import entity.expr.Expr
import pipeline.interpreter.IdentMapper
import pipeline.interpreter.IdentMapperInterface
import pipeline.symbol_table.SymbolTable
import utils.CommonUtils

class TriangleNotation(var p1: String, var p2: String, var p3: String) : Notation() {
    override fun toString(): String = "$p1$p2$p3"
    override fun getRepr(): StringBuilder = StringBuilder("AAA")
    override fun getOrder(): Int = 8
    override fun getPointsAndCircles(): MutableList<String> = mutableListOf(p1, p2, p3)

    override fun mergeMapping(mapper: IdentMapper, other: Notation) {
        other as TriangleNotation
        mapper.mergeMapping(p1, listOf(other.p1))
        mapper.mergeMapping(p3, listOf(other.p3))
        mapper.mergeMapping(p2, listOf(other.p2))
    }

    override fun createLinks(mapper: IdentMapper) {}


    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface): Expr {
        return TriangleNotation(mapper.get(p1), mapper.get(p2), mapper.get(p3))
    }

    override fun compareTo(other: Expr): Int = super.compareOrSame(other) ?: toString().compareTo(other.toString())

    override fun renameToMinimalAndRemap(symbolTable: SymbolTable) {
        p1 = symbolTable.equalIdentRenamer.getIdentical(p1)
        p2 = symbolTable.equalIdentRenamer.getIdentical(p2)
        p3 = symbolTable.equalIdentRenamer.getIdentical(p3)
    }
}
