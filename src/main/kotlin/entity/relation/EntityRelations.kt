package entity.relation

import entity.expr.notation.Notation
import pipeline.SymbolTable

abstract class EntityRelations {
    /**
     * if [other] == null, then a key is already removed.
     * In this case, [otherRelations] might be not null
     */
    abstract fun merge(
        other: Notation?,
        symbolTable: SymbolTable,
        otherRelations: EntityRelations? = null
    )
}
