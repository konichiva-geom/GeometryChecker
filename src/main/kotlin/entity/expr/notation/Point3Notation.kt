package entity.expr.notation

import entity.expr.Expr
import entity.relation.AngleRelations
import pipeline.SymbolTable
import pipeline.interpreter.IdentMapper
import utils.Utils

class Point3Notation(var p1: String, var p2: String, var p3: String) : RelatableNotation() {
    init {
        Utils.sortAngle(this)
    }

    override fun getOrder(): Int = 6

    override fun compareTo(other: Expr): Int = super.compareOrSame(other) ?: toString().compareTo(other.toString())

    override fun getRepr() = StringBuilder("AAA")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapper) = Point3Notation(mapper.get(p1), mapper.get(p2), mapper.get(p3))
    override fun toString(): String = "$p1$p2$p3"

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
        var relations: AngleRelations? = null
        if (symbolTable.angles[this] != null) {
            relations = symbolTable.angles[this]
            symbolTable.angles.remove(this)
        }

        p1 = symbolTable.equalIdentRenamer.getIdentical(p1)
        p2 = symbolTable.equalIdentRenamer.getIdentical(p2)
        p3 = symbolTable.equalIdentRenamer.getIdentical(p3)
        Utils.sortAngle(this)

        if (relations != null)
            symbolTable.angles[this] = relations
    }
}
