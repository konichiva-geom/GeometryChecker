package entity.expr.notation

import entity.expr.Expr
import pipeline.symbol_table.SymbolTable
import pipeline.interpreter.IdentMapper
import pipeline.interpreter.IdentMapperInterface
import utils.CommonUtils

class Point3Notation(var p1: String, var p2: String, var p3: String) : RelatableNotation() {
    init {
        CommonUtils.sortAngle(this)
    }

    override fun getOrder(): Int = 6

    override fun compareTo(other: Expr): Int = super.compareOrSame(other) ?: toString().compareTo(other.toString())

    override fun getRepr() = StringBuilder("∠AAA")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface) =
        Point3Notation(mapper.get(p1), mapper.get(p2), mapper.get(p3))

    override fun toString(): String = "∠$p1$p2$p3"

    override fun getPointsAndCircles(): MutableList<String> = mutableListOf(p1, p2, p3)
    override fun mergeMapping(mapper: IdentMapper, other: Notation) {
        other as Point3Notation
        mapper.mergeMapping(p1, listOf(other.p1, other.p3))
        mapper.mergeMapping(p3, listOf(other.p1, other.p3))
        mapper.mergeMapping(p2, listOf(other.p2))
    }

    override fun createLinks(mapper: IdentMapper) {
        mapper.addLink(p1, p3)
    }

    override fun renameToMinimalAndRemap(symbolTable: SymbolTable) {
        p1 = symbolTable.equalIdentRenamer.getIdentical(p1)
        p2 = symbolTable.equalIdentRenamer.getIdentical(p2)
        p3 = symbolTable.equalIdentRenamer.getIdentical(p3)
        CommonUtils.sortAngle(this)
    }

    override fun hashCode(): Int {
        return (p2 + (listOf(p1, p3).sorted()).joinToString(separator = "")).hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other == null)
            return false
        if (this::class != other::class)
            return false
        other as Point3Notation
        return (p2 + (listOf(p1, p3).sorted()).joinToString(separator = "")) ==
                (other.p2 + (listOf(other.p1, other.p3).sorted()).joinToString(separator = ""))
    }
}
