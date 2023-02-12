package math

import entity.expr.Expr
import entity.expr.notation.Notation
import pipeline.ArithmeticExpander.getArithmeticToString
import pipeline.SymbolTable
import pipeline.interpreter.IdentMapper

class DivNotation(val numerator: MutableMap<Notation, Fraction>, val denominator: MutableMap<Notation, Fraction>) :
    Notation() {
    override fun getOrder() = Int.MAX_VALUE
    override fun getLetters(): MutableList<String> = TODO("Not yet implemented")
    override fun mergeMapping(mapper: IdentMapper, other: Notation) = TODO("Not yet implemented")
    override fun createLinks(mapper: IdentMapper) = TODO("Not yet implemented")
    override fun getRepr() = TODO("Not yet implemented")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapper) = TODO("Not yet implemented")
    override fun compareTo(other: Expr) = TODO("Not yet implemented")
    override fun renameToMinimalAndRemap(symbolTable: SymbolTable) = TODO("Not yet implemented")

    override fun toString(): String {
        if (denominator.isEmpty())
            return getArithmeticToString(numerator).toString()
        var numeratorString =
            if (numerator.size == 1) getArithmeticToString(numerator)
            else "(${getArithmeticToString(numerator)})"
        if (numeratorString.isEmpty())
            numeratorString = "1"
        val denominatorString =
            if (denominator.size == 1) getArithmeticToString(denominator) else "(${
                getArithmeticToString(
                    denominator
                )
            })"
        return "$numeratorString/$denominatorString"
    }
}
