package entity.relation

import entity.expr.notation.Notation
import entity.expr.notation.Point2Notation
import pipeline.symbol_table.SymbolTable

open class LineRelations : LinearRelations() {
    override val pointsNotContained: MutableSet<String> = mutableSetOf()
    // not using relations objects, because when they merge, one of them gets deleted
    val parallel = mutableSetOf<Point2Notation>()
    val perpendicular = mutableSetOf<Point2Notation>()

    override fun merge(other: Notation?, symbolTable: SymbolTable, otherRelations: EntityRelations?) {
        val deleted = (otherRelations ?: symbolTable.getLine(other as Point2Notation)) as LineRelations
        mergeDifferentPoints(deleted)
        parallel.addAll(deleted.parallel)
        perpendicular.addAll(deleted.parallel)
        if (other != null)
            symbolTable.resetLine(this, other as Point2Notation)
    }
}
