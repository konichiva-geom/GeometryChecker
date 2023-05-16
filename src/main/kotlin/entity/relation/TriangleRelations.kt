package entity.relation

import entity.expr.binary_expr.NotContainable
import entity.expr.notation.Notation
import entity.expr.notation.TriangleNotation
import pipeline.symbol_table.SymbolTable

class TriangleRelations : EntityRelations(), NotContainable {
    override val pointsNotContained: MutableSet<String> = mutableSetOf()
    val pointsInside: MutableSet<String> = mutableSetOf()
    val similarTriangles = mutableListOf<TriangleRelations>()
    override fun merge(other: Notation?, symbolTable: SymbolTable, otherRelations: EntityRelations?) {
        val relations = otherRelations as TriangleRelations? ?: symbolTable.getTriangle(other as TriangleNotation)
        mergeDifferentPoints(relations)
        this.pointsInside.addAll(relations.pointsInside)
        relations.pointsInside.clear()
    }
}
