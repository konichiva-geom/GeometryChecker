package entity

import notation.Point2Notation
import relations.In
import symbolTable

class Ray():Entity() {
    override fun isIn() {
        TODO("Not yet implemented")
    }

    override fun intersects() {
        TODO("Not yet implemented")
    }

    override fun isPerpendicular() {
        TODO("Not yet implemented")
    }
}
class Segment():Entity() {
    fun inRelation() {
    }

    override fun isIn() {
        TODO("Not yet implemented")
    }

    override fun intersects() {
        TODO("Not yet implemented")
    }

    override fun isPerpendicular() {
        TODO("Not yet implemented")
    }
}

class Point(val distinctSet: MutableSet<String> = mutableSetOf()) : Entity() {
    val lines: MutableMap<String, Boolean> = mutableMapOf()
    val segments: MutableMap<String, Boolean> = mutableMapOf()
    val rays: MutableMap<String, Boolean> = mutableMapOf()

    fun isIn(name: Point2Notation): Boolean {
               In.inMap[this]?.contains(symbolTable.getRay(name.toRayNotation()))
        return In.inMap[this]?.contains(symbolTable.getLine(name))!!
    }

    override fun isIn() {
        TODO("Not yet implemented")
    }

    override fun intersects() {
        TODO("Not yet implemented")
    }

    override fun isPerpendicular() {
        TODO("Not yet implemented")
    }
}