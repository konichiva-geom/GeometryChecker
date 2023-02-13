package entity.point_collection

import entity.Renamable
import entity.expr.notation.Notation
import pipeline.EqualIdentRenamer

interface PointCollection<T : Notation> : Renamable {
    fun getPointsInCollection(): Set<String>

    /**
     * Check that notation can be transformed into this collection
     */
    fun isFromNotation(notation: T): Boolean
    fun addPoints(added: List<String>)

    fun <T> getValueFromMapAndDeleteThisKey(map: MutableMap<out PointCollection<*>, T>): T? {
        var relations: T? = null
        if (map[this] != null) {
            relations = map[this]
            map.remove(this)
        }

        return relations
    }

    fun renamePointSet(set: MutableSet<String>, equalIdentRenamer: EqualIdentRenamer) {
        val newPoints = set.map { equalIdentRenamer.getIdentical(it) }
        set.clear(); set.addAll(newPoints)
    }
}
