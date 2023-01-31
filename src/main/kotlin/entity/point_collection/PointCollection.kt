package entity.point_collection

import entity.relation.EntityRelations
import pipeline.EqualIdentRenamer
import entity.expr.notation.Notation
import entity.Renamable

interface PointCollection<T : Notation> : Renamable {
    fun getPointsInCollection(): Set<String>

    /**
     * Check that notation can be transformed into this collection
     */
    fun isFromNotation(notation: T): Boolean
    fun addPoints(added: List<String>)

    fun <T : EntityRelations?> getRelations(mapWithRelations: MutableMap<out PointCollection<*>, T>): T? {
        var relations: T? = null
        if (mapWithRelations[this] != null) {
            relations = mapWithRelations[this]
            mapWithRelations.remove(this)
        }

        return relations
    }

    fun renamePointSet(set: MutableSet<String>, equalIdentRenamer: EqualIdentRenamer) {
        val newPoints = set.map { equalIdentRenamer.getIdentical(it) }
        set.clear(); set.addAll(newPoints)
    }
}
