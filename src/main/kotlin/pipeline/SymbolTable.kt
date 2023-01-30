import Utils.sortAngle
import entity.*
import expr.*
import pipeline.*
import relations.Vector
import relations.VectorContainer

open class SymbolTable {
    private val points = mutableMapOf<String, PointRelations>()
    val lines = mutableMapOf<LinePointCollection, LineRelations>()
    val rays = mutableMapOf<RayPointCollection, RayRelations>()
    val segments = mutableMapOf<SegmentPointCollection, SegmentRelations>()
    val angles = mutableMapOf<Point3Notation, AngleRelations>()
    val circles = mutableMapOf<IdentNotation, CircleRelations>()
    val arcs = mutableMapOf<ArcPointCollection, ArcRelations>()

    private val segmentVectors = VectorContainer<SegmentPointCollection>()
    private val angleVectors = VectorContainer<Point3Notation>()
    private val arcToAngleMap = mutableMapOf<ArcPointCollection, Point3Notation>()

    val equalIdentRenamer = EqualIdentRenamer()

    fun addSegmentVector(notation: SegmentNotation, vector: Vector) {
        //segmentVectors.vectors[notation] = vector
    }

    fun addArcVector(notation: ArcNotation, vector: Vector) {}
    fun addAngleVector(notation: Point3Notation, vector: Vector) {
    }

    fun getRelationsByNotation(notation: Notation): EntityRelations {
        return getKeyValueByNotation(notation).second
    }

    fun getKeyByNotation(notation: Notation): Any = getKeyValueByNotation(notation).first

    @Suppress("UNCHECKED_CAST")
    fun getKeyValueByNotation(notation: Notation): Pair<Any, EntityRelations> {
        when (notation) {
            is PointNotation ->
                return notation.p to getPoint(notation)

            is RayNotation -> return getKeyValueForLinear<RayRelations, RayNotation, RayPointCollection>(
                notation,
                rays as MutableMap<PointCollection<RayNotation>, RayRelations>,
                arrayOf(notation.p1, mutableSetOf(notation.p2))
            )

            is SegmentNotation -> return getKeyValueForLinear<SegmentRelations, SegmentNotation, SegmentPointCollection>(
                notation,
                segments as MutableMap<PointCollection<SegmentNotation>, SegmentRelations>,
                arrayOf(notation.getLetters().toMutableSet(), mutableSetOf<String>())
            )
            is ArcNotation -> return getKeyValueForLinear<ArcRelations, ArcNotation, ArcPointCollection>(
                notation,
                arcs as MutableMap<PointCollection<ArcNotation>, ArcRelations>,
                arrayOf(notation.getLetters().toMutableSet(), mutableSetOf<String>(), notation.circle)
            )
            is Point2Notation -> return getKeyValueForLinear<LineRelations, Point2Notation, LinePointCollection>(
                notation,
                lines as MutableMap<PointCollection<Point2Notation>, LineRelations>,
                arrayOf(notation.getLetters().toMutableSet())
            )
            is Point3Notation -> return notation to getAngle(notation)
            else -> throw SpoofError(notation.toString())
        }
    }

    private inline fun <reified T : LinearRelations,
            reified N : Notation,
            reified C : PointCollection<N>> getKeyValueForLinear(
        notation: N,
        map: MutableMap<PointCollection<N>, T>,
        constructorArgs: Array<Any>
    ): Pair<PointCollection<N>, T> {
        // have to iterate over all collection, because if we find by key, we can't take the key
        // e.g. can find by RayCollection("A", ("B")), but key is RayCollection("A", ("B", "C"))
        for ((collection, line) in map) {
            if (collection.isFromNotation(notation))
                return collection to line
        }

        val linearRelations = T::class.constructors.first().call()
        val collection = C::class.constructors.first().call(*constructorArgs)
        map[collection] = linearRelations
        equalIdentRenamer.addSubscribers(collection, *notation.getLetters().toTypedArray())
        if (notation is ArcNotation)
            equalIdentRenamer.addSubscribers(collection, notation.circle)

        return collection to linearRelations
    }

    fun setRelationByNotation(relations: EntityRelations, notation: Notation) {
    }

    /**
     * // TODO new: THIS happens only when points are merging =>
     * not a problem that lines are merged implicitly.
     * Should merge them when points are merged
     * (e.g A == B, then find lines that intersect
     * and one contains A, other contains B).
     * TODO ^ check that upper approach works because it is really
     * THE ONLY case when lines merge (lines merge ONLY IF points merge)
     * // TODO change reset to ...
     * // TODO we shouldn't set equal lines implicitly, but how do we know which line to pick if they have same points
     * // TODO and user didn't merge them?
     * @param newRelations value to set
     * @param notation notation to find what value to set
     */
    fun resetLine(newRelations: LineRelations, notation: Point2Notation) {
        resetLinear(lines, notation, newRelations)
    }

    fun resetSegment(newRelations: SegmentRelations, notation: SegmentNotation) {
        resetLinear(segments, notation, newRelations)
    }

    fun resetRay(newRelations: RayRelations, notation: RayNotation) {
        resetLinear(rays, notation, newRelations)
    }

    private fun <T : PointCollection<*>, R : EntityRelations> resetLinear(
        linearCollection: MutableMap<T, R>,
        notation: Point2Notation,
        newRelations: R
    ) {
        for ((searchedNotation, _) in linearCollection) {
            if (searchedNotation.getPointsInCollection().containsAll(notation.getLetters())) {
                linearCollection[searchedNotation] = newRelations
                return
            }
        }
    }

    fun resetPoint(newRelations: PointRelations, notation: PointNotation) {
        points[notation.p] = newRelations
    }

    fun resetAngle(newRelations: AngleRelations, notation: Point3Notation) {
        angles[notation] = newRelations
    }

    fun getPointSetNotationByNotation(notation: Notation): Set<String> {
        return when (notation) {
            is PointNotation -> setOf(notation.p)
            is Point2Notation -> (getKeyValueByNotation(notation).first as PointCollection<*>).getPointsInCollection()
            is Point3Notation -> setOf(notation.p1, notation.p2, notation.p3)
            is IdentNotation -> getCircle(notation).points
            else -> throw SpoofError(notation.toString())
        }
    }

    fun getPointObjectsByNotation(notation: Notation): Set<PointRelations> =
        getPointSetNotationByNotation(notation).map { getPoint(it) }.toSet()

    fun pointsEqual(p1: String, p2: String): Boolean = points[p1] == points[p2]
    fun pointsContain(p1: String, collection: Set<String>): Boolean = collection.any { points[p1] == points[it] }

    /**
     * Make point distinct from all others
     */
    fun newPoint(notation: PointNotation): PointRelations {
        if (points[notation.p] != null)
            throw SpoofError("Point %{name} already defined", "name" to notation.p)
        points[notation.p] = PointRelations()
        equalIdentRenamer.addPoint(notation.p)
        return points[notation.p]!!
    }

    fun newCircle(notation: IdentNotation): CircleRelations {
        return CircleRelations()
    }

    fun getPoint(name: String): PointRelations {
        return points[name] ?: throw SpoofError("Point %{name} is not instantiated", "name" to name)
    }

    fun getPoint(pointNotation: PointNotation): PointRelations {
        return getPoint(pointNotation.p)
    }

    fun getLine(notation: Point2Notation): LineRelations {
        return getKeyValueByNotation(notation).second as LineRelations
    }

    fun getCircle(notation: IdentNotation): CircleRelations {
        var res = circles[notation]
        if (res == null) {
            res = CircleRelations()
            circles[notation] = res
        }
        return res
    }

    fun getRay(notation: RayNotation): RayRelations {
        return getKeyValueByNotation(notation).second as RayRelations
    }

    fun getSegment(notation: SegmentNotation): SegmentRelations {
        return getKeyValueByNotation(notation).second as SegmentRelations
    }

    fun getAngle(notation: Point3Notation): AngleRelations {
        sortAngle(notation)
        // if (points[notation.p1] == null || points[notation.p2] == null || points[notation.p3] == null)
        //     throw Exception("Point ${if (points[notation.p1] == null) notation.p1 else if (points[notation.p2] == null) notation.p2 else notation.p3} not found")
        if (angles[notation] != null)
            return angles[notation]!!
        angles[notation] = AngleRelations()
        equalIdentRenamer.addSubscribers(notation, *notation.getLetters().toTypedArray())
        return angles[notation]!!
    }

    fun getArc(notation: ArcNotation): ArcRelations {
        for ((collection, value) in arcs) {
            if (collection.getPointsInCollection().containsAll(notation.getLetters()))
                return value
        }
        val res = ArcRelations()
        arcs[ArcPointCollection(notation.getLetters().toMutableSet(), circle = notation.circle)] = res
        return res
    }

    fun clear() {
        points.clear()
        lines.clear()
        rays.clear()
        segments.clear()
        angles.clear()
        circles.clear()
        arcs.clear()
        arcToAngleMap.clear()
        angleVectors.vectors.clear()
        segmentVectors.vectors.clear()
    }
}
