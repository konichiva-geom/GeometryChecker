package entity

import SymbolTable

interface Renamable {
    /**
     * Remove key from map, rename key and put it back into map.
     * This procedure will put it into cell corresponding to its new hashcode,
     * just renaming won't work
     */
    fun renameAndRemap(symbolTable: SymbolTable)
    fun checkValidityAfterRename()
}
