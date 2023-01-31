package entity.relation

import entity.expr.notation.Notation
import pipeline.SymbolTable

abstract class EntityRelations {
    abstract fun merge(other: Notation, symbolTable: SymbolTable)
}
