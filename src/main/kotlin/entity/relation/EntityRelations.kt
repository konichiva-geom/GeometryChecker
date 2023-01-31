package entity.relation

import SymbolTable
import entity.expr.notation.Notation

abstract class EntityRelations
{
    abstract fun merge(other: Notation, symbolTable: SymbolTable)
}
