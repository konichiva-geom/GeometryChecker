package entity.expr.notation

import pipeline.SymbolTable
import pipeline.interpreter.IdentMapper

class ArcNotation(p1: String, p2: String, var circle: String) : Point2Notation(p1, p2) {
    override fun getOrder(): Int = 4
    override fun toLine() = Point2Notation(p1, p2)
    override fun getRepr() = StringBuilder("arc AA")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapper) = ArcNotation(mapper.get(p1), mapper.get(p2), mapper.get(circle))
    override fun toString(): String = "arc ${super.toString()} of $circle"
    override fun renameToMinimalAndRemap(symbolTable: SymbolTable) {
        super.renameToMinimalAndRemap(symbolTable)
        circle = symbolTable.equalIdentRenamer.getIdentical(circle)
    }
}
