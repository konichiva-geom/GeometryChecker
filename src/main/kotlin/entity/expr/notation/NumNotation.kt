package entity.expr.notation

import entity.expr.Expr
import pipeline.symbol_table.SymbolTable
import pipeline.interpreter.IdentMapper
import pipeline.interpreter.IdentMapperInterface

class NumNotation(val number: Double) : Notation() {
    override fun getOrder(): Int = 0
    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun getRepr() = StringBuilder("0")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface) = NumNotation(number)
    override fun renameToMinimalAndRemap(symbolTable: SymbolTable) {}

    override fun toString(): String = ""
    override fun getPointsAndCircles(): MutableList<String> = mutableListOf()

    override fun mergeMapping(mapper: IdentMapper, other: Notation) {}
    override fun createLinks(mapper: IdentMapper) {}
}
