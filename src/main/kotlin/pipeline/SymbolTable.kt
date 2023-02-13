package pipeline

import entity.expr.notation.*
import entity.point_collection.*
import entity.relation.*
import error.SpoofError
import math.*
import pipeline.inference.InferenceProcessor
import utils.Utils.sortAngle

open class SymbolTable(val inferenceProcessor: InferenceProcessor) {
    private val points = mutableMapOf<String, PointRelations>()
    val lines = mutableMapOf<LinePointCollection, LineRelations>()
    val rays = mutableMapOf<RayPointCollection, RayRelations>()
    val segments = mutableMapOf<SegmentPointCollection, SegmentRelations>()
    val angles = mutableMapOf<Point3Notation, AngleRelations>()
    val circles = mutableMapOf<IdentNotation, CircleRelations>()
    val arcs = mutableMapOf<ArcPointCollection, ArcRelations>()

    val segmentVectors = VectorContainer<SegmentPointCollection>()
    val angleVectors = VectorContainer<Point3Notation>()
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
                arrayOf(notation.getPointsAndCircles().toMutableSet(), mutableSetOf<String>())
            )
            is ArcNotation -> return getKeyValueForLinear<ArcRelations, ArcNotation, ArcPointCollection>(
                notation,
                arcs as MutableMap<PointCollection<ArcNotation>, ArcRelations>,
                arrayOf(notation.getPointsAndCircles().toMutableSet(), mutableSetOf<String>(), notation.circle)
            )
            is Point2Notation -> return getKeyValueForLinear<LineRelations, Point2Notation, LinePointCollection>(
                notation,
                lines as MutableMap<PointCollection<Point2Notation>, LineRelations>,
                arrayOf(notation.getPointsAndCircles().toMutableSet())
            )
            is Point3Notation -> return notation to getAngle(notation)
            is IdentNotation -> return notation to circles[notation]!!
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
        equalIdentRenamer.addSubscribers(collection, *notation.getPointsAndCircles().toTypedArray())
        if (notation is ArcNotation)
            equalIdentRenamer.addSubscribers(collection, notation.circle)

        return collection to linearRelations
    }

    fun setRelationByNotation(relations: EntityRelations, notation: Notation) {
    }

    /**
     * Set newRelations to notation
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
            if (searchedNotation.getPointsInCollection().containsAll(notation.getPointsAndCircles())) {
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
            else -> throw SpoofError("Unexpected notation: %{notation}", "notation" to notation)
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

    fun hasPoint(pointNotation: PointNotation): Boolean {
        return points[pointNotation.p] != null
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
        equalIdentRenamer.addSubscribers(notation, *notation.getPointsAndCircles().toTypedArray())
        return angles[notation]!!
    }

    fun getArc(notation: ArcNotation): ArcRelations {
        for ((collection, value) in arcs) {
            if (collection.getPointsInCollection().containsAll(notation.getPointsAndCircles()))
                return value
        }
        val res = ArcRelations()
        arcs[ArcPointCollection(notation.getPointsAndCircles().toMutableSet(), circle = notation.circle)] = res
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

        equalIdentRenamer.clear()
    }

    fun getOrCreateVector(notation: Notation): Vector {
        when (notation) {
            is NumNotation -> return mutableMapOf(setOf(0) to if (notation.number.isZero()) FractionFactory.one() else notation.number)
            is ArcNotation -> {
                val angle = arcToAngleMap[ArcPointCollection(
                    notation.getPointsAndCircles().toMutableSet(), circle = notation.circle
                )] ?: throw SpoofError("Angle for arc %{arc} not specified", "arc" to notation)
                return angleVectors.getOrCreate(angle)
            }
            is Point3Notation -> return angleVectors.getOrCreate(notation)
            is SegmentNotation -> return segmentVectors.getOrCreate(
                SegmentPointCollection(
                    notation.getPointsAndCircles().toMutableSet()
                )
            )
            is MulNotation -> {
                return notation.elements.map {
                    getOrCreateVector(it)
                }.fold(mutableMapOf(setOf<Int>() to FractionFactory.one())) { acc, i ->
                    acc.mergeWith(i, "*")
                }
            }
            else -> throw SpoofError(
                "Unexpected notation %{notation} in arithmetic expression",
                "notation" to notation
            )
        }
    }
}
