package entity.point_collection

import entity.Renamable
import entity.expr.notation.SegmentNotation
import entity.relation.EntityRelations
import error.SpoofError
import math.mergeWithOperation
import pipeline.SymbolTable

open class SegmentPointCollection internal constructor(
    protected val bounds: MutableSet<String>,
    protected val points: MutableSet<String> = mutableSetOf()
) :
    PointCollection<SegmentNotation>() {
    override fun getPointsInCollection(): Set<String> = bounds + points
    override fun isFromNotation(notation: SegmentNotation) = bounds.containsAll(notation.getPointsAndCircles())

    override fun addPoints(added: List<String>, symbolTable: SymbolTable) {
        val relations = symbolTable.segments.remove(this)!!
        symbolTable.equalIdentRenamer.removeSubscribers(this, *added.toTypedArray())
        points.addAll(added)
        symbolTable.equalIdentRenamer.addSubscribers(this as Renamable, *added.toTypedArray())
        symbolTable.segments[this] = relations
    }

    override fun renameToMinimalAndRemap(symbolTable: SymbolTable) {
        val segmentRelations = getValueFromMap(symbolTable.segments, this)
        val vector = getValueFromMap(symbolTable.segmentVectors.vectors, this)

        renamePointSet(bounds, symbolTable.equalIdentRenamer)
        renamePointSet(points, symbolTable.equalIdentRenamer)

        setRelationsInMapIfNotNull(symbolTable.segments, symbolTable, segmentRelations)
        addToMap(vector, symbolTable.segmentVectors, this)
    }

    override fun checkValidityAfterRename(): Exception? {
        if (bounds.size < 2)
            return SpoofError("Cannot use notation with same points: %{point}%{point}", "point" to points.first())
        return null
    }

    override fun toString(): String = "${bounds.joinToString(separator = "")}:$points"

    override fun equals(other: Any?): Boolean {
        if (other !is SegmentPointCollection)
            return false
        return bounds.intersect(other.bounds).size == bounds.size
    }

    override fun hashCode(): Int {
        return bounds.hashCode()
    }

    override fun merge(other: PointCollection<*>, symbolTable: SymbolTable) {
        other as SegmentPointCollection
        assert(bounds == other.bounds)
        points.addAll(other.points)
    }
}
