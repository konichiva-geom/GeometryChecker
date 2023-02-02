package entity.expr.notation

import entity.expr.Expr
import math.Fraction
import math.asString
import pipeline.SymbolTable
import pipeline.interpreter.IdentMapper

class NumNotation(val number: Fraction) : Notation() {
    override fun getOrder(): Int = 0
    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun getRepr() = StringBuilder("0")
    override fun mapIdents(mapper: IdentMapper) = NumNotation(number)
    override fun renameAndRemap(symbolTable: SymbolTable) {}

    override fun toString(): String = ""
    override fun getLetters(): MutableList<String> = mutableListOf()

    override fun mergeMapping(mapper: IdentMapper, other: Notation) {}
    override fun createLinks(mapper: IdentMapper) {}
}
