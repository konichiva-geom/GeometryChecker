import Utils.sortAngle
import entity.AngleRelations
import entity.ArcRelations
import entity.CircleRelations
import entity.EntityRelations
import entity.LineRelations
import entity.PointRelations
import entity.RayRelations
import entity.SegmentRelations
import expr.ArcNotation
import expr.BinaryEquals
import expr.BinaryIn
import expr.BinaryIntersects
import expr.BinaryParallel
import expr.Expr
import expr.IdentNotation
import expr.Notation
import expr.Point2Notation
import expr.Point3Notation
import expr.PointNotation
import expr.RayNotation
import expr.SegmentNotation

interface PointCollection {
    fun getPointsInCollection(): Set<String>
    fun addPoints(added: List<String>)
}

data class LinePointCollection(val points: MutableSet<String>) : PointCollection {
    override fun getPointsInCollection(): Set<String> = points
    override fun addPoints(added: List<String>) {
        points.addAll(added)
    }
}

data class RayPointCollection(val start: String, val points: MutableSet<String>) : PointCollection {
    override fun getPointsInCollection(): Set<String> = setOf(start) + points
    override fun addPoints(added: List<String>) {
        // TODO is it bad if added point is [start]?
        points.addAll(added)
    }
}

data class SegmentPointCollection(val bounds: Set<String>, val points: MutableSet<String> = mutableSetOf()) :
    PointCollection {
    override fun getPointsInCollection(): Set<String> = bounds + points
    override fun addPoints(added: List<String>) {
        // TODO is it bad if added point is in [bounds]?
        points.addAll(points)
    }
}

open class SymbolTable {
    private val points = mutableMapOf<String, PointRelations>()
    val lines = mutableMapOf<LinePointCollection, LineRelations>()
    val rays = mutableMapOf<RayPointCollection, RayRelations>()
    val segments = mutableMapOf<SegmentPointCollection, SegmentRelations>()
    private val angles = mutableMapOf<Point3Notation, AngleRelations>()
    private val circles = mutableMapOf<IdentNotation, CircleRelations>()
    private val arcs = mutableMapOf<SegmentPointCollection, ArcRelations>()
    private val comparisons = mutableMapOf<Notation, Vector<Int>>()

    fun getRelationsByNotation(notation: Notation): EntityRelations {
        return getKeyValueByNotation(notation).second
    }

    fun getKeyByNotation(notation: Notation): Any = getKeyValueByNotation(notation).first

    fun getKeyValueByNotation(notation: Notation): Pair<Any, EntityRelations> {
        when (notation) {
            is PointNotation -> return notation.p to getPoint(notation)
            is RayNotation -> {
                for ((collection, ray) in rays) {
                    if (pointsEqual(collection.start, notation.p1) && pointsContain(notation.p2, collection.points))
                        return collection to ray
                }
                val rayRelations = RayRelations()
                val collection = RayPointCollection(notation.p1, mutableSetOf(notation.p2))
                rays[collection] = rayRelations
                return collection to rayRelations
            }
            is SegmentNotation -> {
                for ((collection, segment) in segments) {
                    if (pointsContain(notation.p1, collection.bounds) && pointsContain(notation.p2, collection.bounds))
                        return collection to segment
                }
                val segmentRelations = SegmentRelations()
                val collection =
                    SegmentPointCollection(notation.getLetters().toMutableSet(), notation.getLetters().toMutableSet())
                segments[collection] = segmentRelations
                return collection to segmentRelations
            }
            is ArcNotation -> {
                for ((collection, value) in arcs) {
                    if (pointsContain(notation.p1, collection.bounds) && pointsContain(notation.p2, collection.bounds))
                        return collection to value
                }
                val res = ArcRelations()
                val collection =
                    SegmentPointCollection(notation.getLetters().toMutableSet(), notation.getLetters().toMutableSet())
                arcs[collection] = res
                return collection to res
            }
            is Point2Notation -> {
                for ((collection, line) in lines) {
                    if (pointsContain(notation.p1, collection.points) && pointsContain(notation.p2, collection.points))
                        return collection to line
                }
                val lineRelations = LineRelations()
                val collection =
                    LinePointCollection(notation.getLetters().toMutableSet())
                lines[collection] = lineRelations
                return collection to lineRelations
            }

            is Point3Notation -> return notation to getAngle(notation)
            else -> throw SpoofError(notation.toString())
        }
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
        for ((searchedNotation, _) in lines) {
            if (searchedNotation.getPointsInCollection().containsAll(notation.getLetters())) {
                lines[searchedNotation] = newRelations
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
            is Point2Notation -> (getKeyValueByNotation(notation).first as PointCollection).getPointsInCollection()
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
        return angles[notation]!!
    }

    fun addRelation(expr: Expr) {
        when (expr) {
            is BinaryEquals -> {}
            is BinaryIn -> {}
            is BinaryIntersects -> {}
            is BinaryParallel -> {}
        }
    }

    fun getArc(notation: ArcNotation): ArcRelations {
        for ((collection, value) in arcs) {
            if (collection.getPointsInCollection().containsAll(notation.getLetters()))
                return value
        }
        val res = ArcRelations()
        arcs[SegmentPointCollection(notation.getLetters().toSet())] = res
        return res
    }
}
