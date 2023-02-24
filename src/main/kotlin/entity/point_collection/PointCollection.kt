package entity.point_collection

import entity.Renamable
import entity.expr.notation.Notation
import entity.relation.EntityRelations
import math.Vector
import math.VectorContainer
import math.mergeWithOperation
import pipeline.EqualIdentRenamer
import pipeline.SymbolTable
import utils.MutablePair
import java.util.*

/**
 * Represents an object, subsequently contains some of its relations.
 *
 * E.g. [LinePointCollection] contains all points, therefore `A in BC` relation is checked from it.
 */
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
     * If there exists the same object, it is merged with [this], then it gets removed and disposed
     */
    protected fun <P : PointCollection<*>, T : EntityRelations> mergeEntitiesInList(
        list: LinkedList<MutablePair<P, T>>,
        symbolTable: SymbolTable
    ) {
        val listOfFoundPairs = mutableListOf<MutablePair<P, T>>()
        for (pair in list)
            if (pair.e1 == this)
                listOfFoundPairs.add(pair)

        assert(listOfFoundPairs.size in 1..2)
        if (listOfFoundPairs.size == 1)
            return

        val (current, disposed) = if (listOfFoundPairs.first().e1 === this)
            listOfFoundPairs.first() to listOfFoundPairs.last()
        else listOfFoundPairs.last() to listOfFoundPairs.first()

        disposed.e1.dispose(symbolTable)
        val iter = list.iterator()
        while (iter.hasNext()) {
            val pair = iter.next()
            if (pair.e1 === disposed.e1) {
                iter.remove()
                break
            }
        }

        current.e1.merge(disposed.e1, symbolTable)
        current.e2.merge(null, symbolTable, disposed.e2)
    }

    fun renamePointSet(set: MutableSet<String>, equalIdentRenamer: EqualIdentRenamer) {
        val newPoints = set.map { equalIdentRenamer.getIdentical(it) }
        set.clear(); set.addAll(newPoints)
    }

    protected fun <T> getValueFromMap(map: MutableMap<out PointCollection<*>, T>, collection: PointCollection<*>): T? {
        var relations: T? = null
        if (map[collection] != null) {
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

    protected fun <T : PointCollection<*>> addToMap(
        vector: Vector?,
        container: VectorContainer<T>,
        collection: PointCollection<*>
    ) {
        if (vector == null)
            return
        for ((angle, vec) in container.vectors) {
            if (angle == collection) {
                container.resolveVector(vector.mergeWithOperation(vec, "-"))
                return
            }
        }
        container.vectors[collection as T] = vector
    }
}
