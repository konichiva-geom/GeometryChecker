package entity.point_collection

import entity.Renamable
import entity.expr.notation.Point2Notation
import error.SpoofError
import pipeline.SymbolTable

class LinePointCollection(private val points: MutableSet<String>) : PointCollection<Point2Notation>() {
    override fun getPointsInCollection(): Set<String> = points
    override fun isFromNotation(notation: Point2Notation) = points.containsAll(notation.getPointsAndCircles())

    override fun addPoints(added: List<String>, symbolTable: SymbolTable) {
        val relations = symbolTable.lines.remove(this)!!
        symbolTable.equalIdentRenamer.removeSubscribers(this, *added.toTypedArray())
        points.addAll(added)
        symbolTable.equalIdentRenamer.addSubscribers(this as Renamable, *added.toTypedArray())
//        for(line in symbolTable.lines.keys) {
//            if(line == this) {
//
//            }
//        }
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

    override fun merge(other: PointCollection<*>, symbolTable: SymbolTable) {
        other as LinePointCollection
        points.addAll(other.points)
    }
}
