package entity.point_collection

import entity.expr.notation.SegmentNotation
import error.SpoofError
import pipeline.SymbolTable
import utils.ExtensionUtils.addOrCreateVectorWithDivision

open class SegmentPointCollection internal constructor(
    protected val bounds: MutableSet<String>,
    protected val points: MutableSet<String> = mutableSetOf()
) :
    PointCollection<SegmentNotation>() {
    override fun getPointsInCollection(): Set<String> = bounds + points
    override fun isFromNotation(notation: SegmentNotation) = bounds.containsAll(notation.getPointsAndCircles())

    override fun addPoints(added: List<String>, symbolTable: SymbolTable) {
        val relations = symbolTable.segments.remove(this)!!
        points.addAll(added)
        symbolTable.segments[this] = relations
    }

    override fun renameToMinimalAndRemap(symbolTable: SymbolTable) {
        val segmentRelations = getValueFromMapAndDeleteThisKey(symbolTable.segments)
        val vector = getValueFromMapAndDeleteThisKey(symbolTable.segmentVectors.vectors)

        renamePointSet(bounds, symbolTable.equalIdentRenamer)
        renamePointSet(points, symbolTable.equalIdentRenamer)

        setRelationsInMapIfNotNull(symbolTable.segments, symbolTable, segmentRelations)
        // TODO: same for incomplete vectors in VectorContainer
        if (vector != null)
            symbolTable.segmentVectors.vectors.addOrCreateVectorWithDivision(this, vector)
    }

    override fun checkValidityAfterRename() {
        if (bounds.size < 2)
            throw SpoofError("Cannot use notation with same points: %{point}%{point}", "point" to points.first())
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

    override fun merge(other: PointCollection<*>) {
        other as SegmentPointCollection
        assert(bounds == other.bounds)
        points.addAll(other.points)
    }
}
