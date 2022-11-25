package entity

import Expr

abstract class Entity // A, |>ABC, BC, AOB
{
    abstract fun isIn()
    abstract fun intersects()
    abstract fun isPerpendicular()
}