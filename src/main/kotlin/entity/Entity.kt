package entity

import notation.Notation

abstract class Entity // A, |>ABC, BC, AOB
{
    abstract fun isIn(other: Notation): Boolean
    abstract fun intersects(): Boolean
    abstract fun isPerpendicular(): Boolean
}