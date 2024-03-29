package entity.expr.notation

import entity.expr.Expr
import entity.relation.CircleRelations
import pipeline.symbol_table.SymbolTable
import pipeline.interpreter.IdentMapper
import pipeline.interpreter.IdentMapperInterface

class IdentNotation(var text: String) : RelatableNotation() {
    override fun getOrder(): Int = 7
    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun getRepr() = StringBuilder("c")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface) = IdentNotation(mapper.get(text))
    override fun toString(): String = text
    override fun getPointsAndCircles(): MutableList<String> = mutableListOf(text)
    override fun mergeMapping(mapper: IdentMapper, other: Notation) {
        mapper.mergeMapping(text, listOf((other as IdentNotation).text))
    }

    override fun createLinks(mapper: IdentMapper) {}

    override fun renameToMinimalAndRemap(symbolTable: SymbolTable) {
        var circleRelations: CircleRelations? = null
        if (symbolTable.circles[this] != null) {
            circleRelations = symbolTable.circles[this]
            symbolTable.circles.remove(this)
        }

        text = symbolTable.equalIdentRenamer.getIdentical(text)

        if (circleRelations != null)
            symbolTable.circles[this] = circleRelations
    }

    override fun hashCode(): Int {
        return text.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is IdentNotation)
            return false
        return text == other.text
    }
}
