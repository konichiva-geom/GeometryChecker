package entity

import notation.Notation

//class entity.Angle(left:entity.Point, middle: entity.Point, right: entity.Point): Term
class Angle: Entity() {
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