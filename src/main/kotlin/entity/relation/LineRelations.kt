package entity.relation

import SymbolTable
import entity.expr.notation.Notation
import entity.expr.notation.Point2Notation

open class LineRelations : LinearRelations() {
    // not using relations objects, because when they merge, one of them gets deleted
    val parallel = mutableSetOf<Point2Notation>()
    val perpendicular = mutableSetOf<Point2Notation>()

    override fun merge(other: Notation, symbolTable: SymbolTable) {
        val deleted = symbolTable.getLine(other as Point2Notation)
        mergeDifferentPoints(deleted)
        parallel.addAll(deleted.parallel)
        perpendicular.addAll(deleted.parallel)
        symbolTable.resetLine(this, other)
    }
}
