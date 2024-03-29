package entity

import pipeline.symbol_table.SymbolTable


interface Renamable {
    /**
     * Remove key from map, rename key to minimal same and put it back into map.
     * This procedure will put it into cell corresponding to its new hashcode,
     * just renaming won't work
     */
    fun renameToMinimalAndRemap(symbolTable: SymbolTable)
    fun checkValidityAfterRename(): Exception?
}
