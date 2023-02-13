package entity.expr.notation

import entity.expr.Expr
import pipeline.SymbolTable
import pipeline.interpreter.IdentMapper
import utils.MathUtils
import utils.Utils

open class Point2Notation(p1: String, p2: String) : RelatableNotation() {
    var p1: String
    var p2: String

    /**
     * cannot call sortLine() here, because  it will be called inside RayNotation constructor too,
     * destroying ray structure
     */
    init {
        this.p1 = MathUtils.min(p1, p2)
        this.p2 = MathUtils.max(p1, p2)
    }

    override fun getOrder(): Int = 5

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun getRepr() = StringBuilder("line AA")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapper) = Point2Notation(mapper.get(p1), mapper.get(p2))
    override fun toString(): String = "line $p1$p2"
    override fun getPointsAndCircles(): MutableList<String> = mutableListOf(p1, p2)
    override fun mergeMapping(mapper: IdentMapper, other: Notation) {
        other as Point2Notation
        mapper.mergeMapping(p1, other.getPointsAndCircles())
        mapper.mergeMapping(p2, other.getPointsAndCircles())
    }

    override fun createLinks(mapper: IdentMapper) {
        mapper.addLink(p1, p2)
    }

    fun toRayNotation() = RayNotation(p1, p2)
    fun toSegmentNotation() = SegmentNotation(p1, p2)
    open fun toLine() = this

    override fun renameToMinimalAndRemap(symbolTable: SymbolTable) {
        p1 = symbolTable.equalIdentRenamer.getIdentical(p1)
        p2 = symbolTable.equalIdentRenamer.getIdentical(p2)
        Utils.sortLine(this)
    }
}
