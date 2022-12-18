package entity

import SymbolTable
import expr.Notation
import expr.Point2Notation
import relations.In
import symbolTable

class RayRelations : LineRelations() {
    override fun isIn(other: Notation): Boolean {
        TODO("Not yet implemented")
    }

    override fun intersects(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isPerpendicular(): Boolean {
        TODO("Not yet implemented")
    }
}

class SegmentRelations : LineRelations() {
    fun inRelation() {
    }

    override fun isIn(other: Notation): Boolean {
        TODO("Not yet implemented")
    }

    override fun intersects(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isPerpendicular(): Boolean {
        TODO("Not yet implemented")
    }
}

class Point(val distinctSet: MutableSet<String> = mutableSetOf()) : EntityRelations() {
    val lines: MutableMap<String, Boolean> = mutableMapOf()
    val segments: MutableMap<String, Boolean> = mutableMapOf()
    val rays: MutableMap<String, Boolean> = mutableMapOf()

    fun isIn(name: Point2Notation): Boolean {
        In.inMap[this]?.contains(symbolTable.getRay(name.toRayNotation()))
        return In.inMap[this]?.contains(symbolTable.getLine(name))!!
    }

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
        TODO("Not yet implemented")
    }
}