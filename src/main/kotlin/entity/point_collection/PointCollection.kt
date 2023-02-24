package entity.point_collection

import entity.Renamable
import entity.expr.notation.Notation
import entity.relation.EntityRelations
import pipeline.EqualIdentRenamer
import pipeline.SymbolTable

abstract class PointCollection<T : Notation> : Renamable {
    abstract fun getPointsInCollection(): Set<String>

    /**
     * Check that notation can be transformed into this collection
     */
    abstract fun isFromNotation(notation: T): Boolean
    abstract fun addPoints(added: List<String>, symbolTable: SymbolTable)

    /**
     * Method to merge point sets when two collections become equal due to the points becoming equal.
     * E.g. there is [SymbolTable].segments = ({A, B, C}, {A, D, E}). Then B == D. Before remapping relations, make sure that
     * [SymbolTable].segments is now ({A, B, C, E})
     */
    abstract fun merge(other: PointCollection<*>, symbolTable: SymbolTable)
    fun dispose(symbolTable: SymbolTable) {
        symbolTable.equalIdentRenamer.removeSubscribers(this, *getPointsInCollection().toTypedArray())
    }

    /**
     * TODO: it is important that [this] is contained within a key (we don't have to merge key with [this]).
     * Because [this] should always be a collection from notation (or a collection from a set).
     * If it changes, change it in [RayPointCollection] too.
     */
    protected fun <T> getValueFromMapAndDeleteThisKey(map: MutableMap<out PointCollection<*>, T>): T? {
        var relations: T? = null
        if (map[this] != null) {
            relations = map[this]
            map.remove(this)
        }

        return relations
    }

    protected fun <T : EntityRelations> setRelationsInMapIfNotNull(
        map: MutableMap<out PointCollection<*>, T>,
        symbolTable: SymbolTable,
        relations: EntityRelations?
    ) {
        if (relations != null) {
            if (map[this] != null) {
                val mergedKey = map.keys.find { it.hashCode() == this.hashCode() }!!
                val oldRelations = map.remove(this)!!
                relations.merge(null, symbolTable, oldRelations)
                mergedKey.dispose(symbolTable)
                this.merge(mergedKey, symbolTable)
                setRelationsInMap(map as MutableMap<PointCollection<*>, EntityRelations>, relations)
            } else setRelationsInMap(map as MutableMap<PointCollection<*>, EntityRelations>, relations)
        }
    }

    private fun <T : EntityRelations> setRelationsInMap(map: MutableMap<PointCollection<*>, T>, relations: T) {
        map[this] = relations
    }

    fun renamePointSet(set: MutableSet<String>, equalIdentRenamer: EqualIdentRenamer) {
        val newPoints = set.map { equalIdentRenamer.getIdentical(it) }
        set.clear(); set.addAll(newPoints)
    }
}
