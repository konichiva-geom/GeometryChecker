package entity.point_collection

import SpoofError
import SymbolTable
import entity.expr.notation.SegmentNotation

open class SegmentPointCollection(val bounds: MutableSet<String>, val points: MutableSet<String> = mutableSetOf()) :
    PointCollection<SegmentNotation> {
    override fun getPointsInCollection(): Set<String> = bounds + points
    override fun isFromNotation(notation: SegmentNotation) = bounds.containsAll(notation.getLetters())

    override fun addPoints(added: List<String>) {
        // TODO is it bad if added point is in [bounds]?
        points.addAll(added)
    }

    override fun renameAndRemap(symbolTable: SymbolTable) {
        val segmentRelations = getRelations(symbolTable.segments)

        renamePointSet(bounds, symbolTable.equalIdentRenamer)
        renamePointSet(points, symbolTable.equalIdentRenamer)

        if (segmentRelations != null)
            symbolTable.segments[this] = segmentRelations
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
}
