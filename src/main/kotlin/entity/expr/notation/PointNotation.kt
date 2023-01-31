package entity.expr.notation

import SymbolTable
import expr.Expr
import pipeline.interpreter.IdentMapper

class PointNotation(var p: String) : RelatableNotation() {
    override fun getOrder(): Int = 1

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun getRepr() = StringBuilder("A")
    override fun mapIdents(mapper: IdentMapper) = PointNotation(mapper.get(p))
    override fun renameAndRemap(symbolTable: SymbolTable) {
        p = symbolTable.equalIdentRenamer.getIdentical(p)
    }

    override fun toString(): String = p
    override fun getLetters(): MutableList<String> = mutableListOf(p)
    override fun mergeMapping(mapper: IdentMapper, other: Notation) {
        other as PointNotation
        mapper.mergeMapping(p, listOf(other.p))
    }

    override fun createLinks(mapper: IdentMapper) {}
}
