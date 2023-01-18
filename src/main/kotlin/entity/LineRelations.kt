package entity

import SymbolTable
import expr.Notation
import expr.Point2Notation

open class LineRelations : LinearRelations() {

    val parallel =
        mutableSetOf<Point2Notation>() // not using relations objects, because when they merge, one of them gets deleted
    val perpendicular = mutableSetOf<Point2Notation>()

    override fun merge(other: Notation, symbolTable: SymbolTable) {
        val deleted = symbolTable.getLine(other as Point2Notation)
        mergeDifferentPoints(deleted)
        parallel.addAll(deleted.parallel)
        perpendicular.addAll(deleted.parallel)
        symbolTable.resetLine(this, other)
    }
}

open class LinearRelations : EntityRelations() {
    // points not in linear
    protected val differentPoints = mutableSetOf<String>()

    override fun merge(other: Notation, symbolTable: SymbolTable) {
        TODO("Not yet implemented")
    }

    protected fun mergeDifferentPoints(other: LinearRelations) {
        this.differentPoints.addAll(other.differentPoints)
        other.differentPoints.clear()
    }
}