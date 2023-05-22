package entity.point_collection

import entity.Renamable
import entity.expr.notation.Notation
import entity.relation.AngleRelations
import entity.relation.EntityRelations
import math.VectorContainer
import math.mergeWithOperation
import pipeline.EqualIdentRenamer
import pipeline.symbol_table.SymbolTable
import utils.MultiSet
import utils.MutablePair
import java.util.*
import kotlin.math.abs

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
     * E.g. there is [SymbolTable].segments = ({A, B, C}, {A, D, E}). Then B == D.
     * Before remapping relations, make sure that
     * [SymbolTable].segments is now ({A, B, C, E})
     */
    abstract fun merge(other: PointCollection<*>, symbolTable: SymbolTable)
    fun dispose(symbolTable: SymbolTable) {
        symbolTable.equalIdentRenamer.removeSubscribers(this as Renamable, *getPointsInCollection().toTypedArray())
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

        //assert(listOfFoundPairs.size in 1..2)
        if (listOfFoundPairs.size == 1)
            return

        val disposedList = listOfFoundPairs.filter { it.e1 !== this }.toMutableList()
        val current = listOfFoundPairs.filter { it.e1 === this }.first()

        var disposed: MutablePair<P, T>
        do {
            disposed = disposedList.removeLast()
            disposed.e1.dispose(symbolTable)
            val iter = list.iterator()
            while (iter.hasNext()) {
                val pair = iter.next()
                if (pair.e1 === disposed.e1) {
                    iter.remove()
                    break
                }
            }

            if (disposed.e1 is RayPointCollection) {
                val raysThatNeedAngleChanges = mutableSetOf(this as RayPointCollection)
                (disposed.e1 as RayPointCollection).angles.forEach {
                    if (it.leftArm === disposed.e1) {
                        it.leftArm = this
                        this.angles.add(it)
                        raysThatNeedAngleChanges.add(it.rightArm)
                    }
                    if (it.rightArm === disposed.e1) {
                        it.rightArm = this
                        this.angles.add(it)
                        raysThatNeedAngleChanges.add(it.leftArm)
                    }
                }
                if (symbolTable.angles.size != symbolTable.angles.map { it.e1 }.toSet().size) {
                    // TODO remove assertion, probably wrong
                    assert(abs(symbolTable.angles.size - symbolTable.angles.map { it.e1 }.toSet().size) == 1)
                    (disposed.e1 as RayPointCollection).angles.forEach {
                        it.mergeEntitiesInList(symbolTable.angles, symbolTable)
                    }
                }

                for (ray in raysThatNeedAngleChanges)
                    ray.removeUnexistingAngles(symbolTable)
            }

            current.e1.merge(disposed.e1, symbolTable)
            current.e2.merge(null, symbolTable, disposed.e2)
        } while (disposedList.isNotEmpty())
    }

    private fun removeAndMergeSpecificEntitiesInList(
        removed: AnglePointCollection,
        merged: AnglePointCollection,
        symbolTable: SymbolTable
    ) {
        val iter = symbolTable.angles.iterator()
        var mergedRelations: AngleRelations? = null
        var removedRelations: AngleRelations? = null
        while (iter.hasNext()) {
            val anglePair = iter.next()
            if (anglePair.e1 === removed) {
                removedRelations = anglePair.e2
                iter.remove()
            } else if (anglePair.e1 === merged)
                mergedRelations = anglePair.e2
        }
        mergedRelations!!.merge(null, symbolTable, removedRelations!!)
    }

    fun renamePointSet(set: MutableSet<String>, equalIdentRenamer: EqualIdentRenamer) {
        val newPoints = set.map { equalIdentRenamer.getIdentical(it) }
        set.clear(); set.addAll(newPoints)
    }

    protected fun <T> removeValueFromMap(
        map: MutableMap<out PointCollection<*>, T>,
        collection: PointCollection<*>
    ): T? {
        var value: T? = null
        if (map[collection] != null) {
            value = map[collection]
            map.remove(collection)
        }

        return value
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
        vector: MutableMap<MultiSet<Int>, Double>?,
        container: VectorContainer<T>,
        collection: PointCollection<*>,
        symbolTable: SymbolTable
    ): Pair<Int, Int>? {
        if (vector == null)
            return null
        val oldAngles = container.vectors.keys
        for (angle in oldAngles) {
            if (angle == collection) {
                val res = if (vector != container.vectors[angle]) {
                    container.resolveVector(vector.mergeWithOperation(container.vectors[angle]!!, "-"))
                } else null
                val vectorChanged = container.vectors[angle]!!
                container.vectors.remove(angle)
                container.vectors[collection as T] = vectorChanged

                if (collection is AnglePointCollection) {
                    removeAndMergeSpecificEntitiesInList(angle as AnglePointCollection, collection, symbolTable)
                }
                return res
            }
        }
        container.vectors[collection as T] = vector
        return null
    }
}
