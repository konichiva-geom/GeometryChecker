package entity.expr.notation

import entity.expr.Expr
import pipeline.symbol_table.SymbolTable
import pipeline.interpreter.IdentMapper
import pipeline.interpreter.IdentMapperInterface

class PointNotation(var p: String) : RelatableNotation() {
    override fun getOrder(): Int = 1

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun getRepr() = StringBuilder("A")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface) = PointNotation(mapper.get(p))
    override fun renameToMinimalAndRemap(symbolTable: SymbolTable) {
        p = symbolTable.equalIdentRenamer.getIdentical(p)
    }

    override fun toString(): String = p
    override fun getPointsAndCircles(): MutableList<String> = mutableListOf(p)
    override fun mergeMapping(mapper: IdentMapper, other: Notation) {
        other as PointNotation
        mapper.mergeMapping(p, listOf(other.p))
    }

    override fun createLinks(mapper: IdentMapper) {}

    override fun hashCode(): Int {
        return p.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is PointNotation)
            return false
        return p == other.p
    }
}
