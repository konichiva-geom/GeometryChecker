package entity.point_collection

import entity.expr.notation.Point2Notation
import error.SpoofError
import pipeline.SymbolTable

class LinePointCollection(private val points: MutableSet<String>) : PointCollection<Point2Notation>() {
    override fun getPointsInCollection(): Set<String> = points
    override fun isFromNotation(notation: Point2Notation) = points.containsAll(notation.getPointsAndCircles())

    override fun addPoints(added: List<String>, symbolTable: SymbolTable) {
        val relations = symbolTable.lines.remove(this)!!
        points.addAll(added)
        symbolTable.lines[this] = relations
    }

    override fun renameToMinimalAndRemap(symbolTable: SymbolTable) {
        val lineRelations = getValueFromMapAndDeleteThisKey(symbolTable.lines)

        renamePointSet(points, symbolTable.equalIdentRenamer)

        setRelationsInMapIfNotNull(symbolTable.lines, symbolTable, lineRelations)
    }

    override fun checkValidityAfterRename(): Exception? {
        if (points.size < 2)
            return SpoofError("Cannot use notation with same points: %{point}%{point}", "point" to points.first())
        return null
    }

    override fun equals(other: Any?): Boolean {
        if (other !is LinePointCollection)
            return false
        return points.intersect(other.points).size >= 2
    }

    override fun hashCode(): Int {
        return points.hashCode()
    }

    override fun toString(): String = "$points"
    override fun merge(other: PointCollection<*>) {
        other as LinePointCollection
        points.addAll(other.points)
    }
}
