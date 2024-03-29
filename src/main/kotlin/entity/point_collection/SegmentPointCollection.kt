package entity.point_collection

import entity.Renamable
import entity.expr.notation.SegmentNotation
import error.SpoofError
import pipeline.symbol_table.SymbolTable

open class SegmentPointCollection internal constructor(
    protected val bounds: MutableSet<String>,
    protected val points: MutableSet<String> = mutableSetOf()
) :
    PointCollection<SegmentNotation>() {
    override fun getPointsInCollection(): Set<String> = bounds + points
    override fun isFromNotation(notation: SegmentNotation) = bounds.containsAll(notation.getPointsAndCircles())

    override fun addPoints(added: List<String>, symbolTable: SymbolTable) {
        val filtered = added.filter {it !in bounds}
        val relations = symbolTable.segments.remove(this)!!
        symbolTable.equalIdentRenamer.removeSubscribers(this, *(points + bounds).toTypedArray())
        points.addAll(filtered)
        symbolTable.equalIdentRenamer.addSubscribers(this as Renamable, *(points + bounds).toTypedArray())
        symbolTable.segments[this] = relations
    }

    override fun renameToMinimalAndRemap(symbolTable: SymbolTable) {
        val segmentRelations = removeValueFromMap(symbolTable.segments, this)
        val vector = removeValueFromMap(symbolTable.segmentVectors.vectors, this)

        renamePointSet(bounds, symbolTable.equalIdentRenamer)
        renamePointSet(points, symbolTable.equalIdentRenamer)

        setRelationsInMapIfNotNull(symbolTable.segments, symbolTable, segmentRelations)
        addToMap(vector, symbolTable.segmentVectors, this, symbolTable)
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
