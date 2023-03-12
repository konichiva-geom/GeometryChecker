package math

import entity.expr.Expr
import entity.expr.notation.Notation
import pipeline.SymbolTable
import pipeline.interpreter.IdentMapper

//class NotationFraction(
//    val numerator: MutableList<Notation>,
//    val denominator: MutableMap<Notation, Fraction> = mutableMapOf()
//) :
//    Notation() {
//
//    override fun getOrder() = Int.MAX_VALUE
//    override fun getLetters(): MutableList<String> = TODO("Not yet implemented")
//    override fun mergeMapping(mapper: IdentMapper, other: Notation) = TODO("Not yet implemented")
//    override fun createLinks(mapper: IdentMapper) = TODO("Not yet implemented")
//    override fun getRepr() = TODO("Not yet implemented")
//    override fun mapIdents(mapper: IdentMapper) = TODO("Not yet implemented")
//    override fun compareTo(other: Expr) = TODO("Not yet implemented")
//    override fun renameAndRemap(symbolTable: SymbolTable) = TODO("Not yet implemented")
//
//    override fun toString(): String {
//        val numerator = numerator.sortedBy { it.getOrder() }.joinToString(separator = " * ")
//        return when (denominator.size) {
//            0 -> numerator
//            1 -> "$numerator / (${denominator.entries.first().value.toString() + denominator.entries.first().key})"
//            else -> {
//                "af"
//            }
//        }
//    }
//
//}

class MulNotation(val elements: MutableList<Notation>) : Notation() {
    override fun getOrder() = Int.MAX_VALUE - 1
    override fun getPointsAndCircles(): MutableList<String> {
        return elements.fold(mutableListOf()) { acc: MutableList<String>, notation ->
            acc.addAll(notation.getPointsAndCircles())
            acc
        }
    }

    override fun mergeMapping(mapper: IdentMapper, other: Notation) = TODO("Not yet implemented")
    override fun createLinks(mapper: IdentMapper) = TODO("Not yet implemented")
    override fun getRepr() = TODO("Not yet implemented")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapper) = TODO("Not yet implemented")
    override fun compareTo(other: Expr) = TODO("Not yet implemented")
    override fun renameToMinimalAndRemap(symbolTable: SymbolTable) {
        elements.forEach { it.renameToMinimalAndRemap(symbolTable) }
    }

    override fun toString(): String {
        return elements.joinToString(separator = "*")
    }
}

