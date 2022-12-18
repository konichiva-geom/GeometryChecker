package entity

import SymbolTable
import expr.Notation
import expr.Point2Notation

open class LineRelations : EntityRelations() {
    val differentPoints = mutableSetOf<String>()
    val parallel =
        mutableSetOf<Point2Notation>() // not using relations objects, because when they merge, one of them gets deleted
    val perpendicular = mutableSetOf<Point2Notation>()

    override fun isIn(other: Notation): Boolean {
        TODO("Not yet implemented")
    }

    override fun intersects(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isPerpendicular(): Boolean {
        TODO("Not yet implemented")
    }

    override fun merge(other: Notation, symbolTable: SymbolTable) {
        val deleted = symbolTable.getLine(other as Point2Notation)
        differentPoints.addAll(deleted.differentPoints)
        parallel.addAll(deleted.parallel)
        perpendicular.addAll(deleted.parallel)
        symbolTable.resetLine(this, other)
    }
}