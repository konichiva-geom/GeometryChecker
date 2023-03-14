package entity.expr.notation

import entity.expr.Expr
import pipeline.symbol_table.SymbolTable
import pipeline.interpreter.IdentMapper

class TupleNotation<T : Notation>(val notations: List<T>) : Notation() {
    override fun getOrder(): Int {
        TODO("Not yet implemented")
    }

    override fun getPointsAndCircles(): MutableList<String> {
        return notations.fold(mutableListOf()) { list, notation ->
            list.addAll(notation.getPointsAndCircles())
            list
        }
    }

    override fun mergeMapping(mapper: IdentMapper, other: Notation) {
        TODO("Not yet implemented")
    }

    override fun createLinks(mapper: IdentMapper) {
        TODO("Not yet implemented")
    }

    override fun getRepr(): StringBuilder {
        val res = StringBuilder("(")
        notations.forEach {
            res.append(it.getRepr())
            res.append(", ")
        }
        res.deleteRange(res.lastIndex - 1, res.length)
        return res.append(")")
    }

    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapper): Expr {
        return TupleNotation(notations.map { it.createNewWithMappedPointsAndCircles(mapper) as Notation })
    }

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun renameToMinimalAndRemap(symbolTable: SymbolTable) {
        notations.forEach { it.renameToMinimalAndRemap(symbolTable) }
    }

    override fun toString(): String = getPointsAndCircles().joinToString(separator = ", ", prefix = "(", postfix = ")")
}