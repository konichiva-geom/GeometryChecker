package math

import entity.expr.Expr
import entity.expr.notation.Notation
import pipeline.ArithmeticExpander.getArithmeticToString
import pipeline.interpreter.IdentMapper
import pipeline.interpreter.IdentMapperInterface
import pipeline.symbol_table.SymbolTable

class DivNotation(val numerator: MutableMap<Notation, Double>, val denominator: MutableMap<Notation, Double>) :
    Notation() {
    override fun getOrder() = Int.MAX_VALUE
    override fun getPointsAndCircles(): MutableList<String> =
        (numerator.keys.map { it.getPointsAndCircles() }
            .flatten() + denominator.keys.map { it.getPointsAndCircles() }.flatten()).toMutableList()

    override fun mergeMapping(mapper: IdentMapper, other: Notation) = TODO("Not yet implemented")
    override fun createLinks(mapper: IdentMapper) = TODO("Not yet implemented")
    override fun getRepr() = TODO("Not yet implemented")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface) = TODO("Not yet implemented")
    override fun compareTo(other: Expr) = TODO("Not yet implemented")
    override fun renameToMinimalAndRemap(symbolTable: SymbolTable) = TODO("Not yet implemented")

    override fun toString(): String {
        if (denominator.isEmpty())
            return getArithmeticToString(numerator).toString()
        var numeratorString = if (numerator.size == 1) getArithmeticToString(numerator)
        else "(${getArithmeticToString(numerator)})"
        if (numeratorString.isEmpty())
            numeratorString = "1"
        val denominatorString =
            if (denominator.size == 1) getArithmeticToString(denominator) else "(${
                getArithmeticToString(denominator)
            })"
        if (denominatorString.toString() == "")
            return numeratorString.toString()
        return "$numeratorString/$denominatorString"
    }

    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }

    override fun equals(other: Any?): Boolean {
        if(other == null || other !is DivNotation)
            return false
        return this === other
    }
}
