package entity.point_collection

import entity.expr.notation.ArcNotation
import error.SpoofError
import pipeline.SymbolTable

class ArcPointCollection(
    val bounds: MutableSet<String>,
    val points: MutableSet<String> = mutableSetOf(),
    var circle: String
) :
    PointCollection<ArcNotation> {
    override fun getPointsInCollection() = bounds + points

    override fun addPoints(added: List<String>) {
        points.addAll(added)
    }

    override fun renameToMinimalAndRemap(symbolTable: SymbolTable) {
        val arcRelations = getRelations(symbolTable.arcs)

        renamePointSet(bounds, symbolTable.equalIdentRenamer)
        renamePointSet(points, symbolTable.equalIdentRenamer)
        circle = symbolTable.equalIdentRenamer.getIdentical(circle)

        if (arcRelations != null)
            symbolTable.arcs[this] = arcRelations
    }

    override fun checkValidityAfterRename() {
        if (points.size < 2)
            throw SpoofError("Cannot use notation with same points: %{point}%{point}", "point" to points.first())
    }

    override fun isFromNotation(notation: ArcNotation) = bounds.containsAll(notation.getLetters())
            && notation.circle == circle

    override fun equals(other: Any?): Boolean {
        if (other !is ArcPointCollection)
            return false
        if (bounds.intersect(other.bounds).size != bounds.size)
            return false
        return circle == other.circle
    }

    override fun hashCode(): Int {
        return bounds.hashCode() + 31 * circle.hashCode()
    }
}
