package entity

import SymbolTable
import expr.Notation

abstract class EntityRelations
{
    abstract fun merge(other: Notation, symbolTable: SymbolTable)
}
