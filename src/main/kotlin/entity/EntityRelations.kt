package entity

import SymbolTable
import expr.Notation

abstract class EntityRelations // A, |>ABC, BC, AOB
{
    abstract fun isIn(other: Notation): Boolean
    abstract fun intersects(): Boolean
    abstract fun isPerpendicular(): Boolean
    abstract fun merge(other: Notation, symbolTable: SymbolTable)
}