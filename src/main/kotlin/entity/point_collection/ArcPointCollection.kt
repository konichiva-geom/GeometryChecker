package entity.point_collection

import entity.expr.notation.ArcNotation
import error.SpoofError
import pipeline.symbol_table.SymbolTable

class ArcPointCollection(
    private val bounds: MutableSet<String>,
    private val points: MutableSet<String> = mutableSetOf(),
    private var circle: String
) :
    PointCollection<ArcNotation>() {
    override fun getPointsInCollection() = bounds + points

    override fun addPoints(added: List<String>, symbolTable: SymbolTable) {
        val relations = symbolTable.arcs.remove(this)!!
        symbolTable.equalIdentRenamer.removeSubscribers(this, *points.toTypedArray())
        points.addAll(added)
        symbolTable.equalIdentRenamer.addSubscribers(this, *points.toTypedArray())
        symbolTable.arcs[this] = relations
    }

    override fun renameToMinimalAndRemap(symbolTable: SymbolTable) {
        val arcRelations = removeValueFromMap(symbolTable.arcs, this)

        renamePointSet(bounds, symbolTable.equalIdentRenamer)
        renamePointSet(points, symbolTable.equalIdentRenamer)
        circle = symbolTable.equalIdentRenamer.getIdentical(circle)

        setRelationsInMapIfNotNull(symbolTable.arcs, symbolTable, arcRelations)
    }

    override fun checkValidityAfterRename(): Exception? {
        if (points.size < 2)
            return SpoofError("Cannot use notation with same points: %{point}%{point}", "point" to points.first())
        return null
    }

    override fun isFromNotation(notation: ArcNotation) = bounds.containsAll(notation.getPointsAndCircles())
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

    override fun merge(other: PointCollection<*>, symbolTable: SymbolTable) {
        other as ArcPointCollection
        assert(circle == other.circle)
        assert(bounds == other.bounds)
        points.addAll(other.points)
    }
}
