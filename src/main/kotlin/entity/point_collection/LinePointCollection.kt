package entity.point_collection

import entity.expr.notation.Point2Notation
import error.SpoofError
import pipeline.SymbolTable

class LinePointCollection(val points: MutableSet<String>) : PointCollection<Point2Notation> {
    override fun getPointsInCollection(): Set<String> = points
    override fun isFromNotation(notation: Point2Notation) = points.containsAll(notation.getPointsAndCircles())

    override fun addPoints(added: List<String>) {
        points.addAll(added)
    }

    override fun renameToMinimalAndRemap(symbolTable: SymbolTable) {
        val lineRelations = getRelations(symbolTable.lines)

        renamePointSet(points, symbolTable.equalIdentRenamer)

        if (lineRelations != null)
            symbolTable.lines[this] = lineRelations
    }

    override fun checkValidityAfterRename() {
        if (points.size < 2)
            throw SpoofError("Cannot use notation with same points: %{point}%{point}", "point" to points.first())
    }

    override fun equals(other: Any?): Boolean {
        if (other !is LinePointCollection)
            return false
        return points.intersect(other.points).size > 2
    }

    override fun hashCode(): Int {
        return points.hashCode()
    }
}
