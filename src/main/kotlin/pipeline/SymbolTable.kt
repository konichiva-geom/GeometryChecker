package pipeline

import entity.expr.notation.*
import entity.point_collection.*
import entity.relation.*
import error.SpoofError
import math.*
import math.Vector
import pipeline.inference.InferenceProcessor
import utils.multiSetOf
import utils.MutablePair
import utils.with
import java.util.*

open class SymbolTable(val inferenceProcessor: InferenceProcessor) {
    private val points = mutableMapOf<String, PointRelations>()

    val lines = LinkedList<MutablePair<LinePointCollection, LineRelations>>()
    val rays = LinkedList<MutablePair<RayPointCollection, RayRelations>>()
    val angles = LinkedList<MutablePair<AnglePointCollection, AngleRelations>>()
    val arcToAngleList = LinkedList<MutablePair<ArcPointCollection, AnglePointCollection>>()

    val segments = mutableMapOf<SegmentPointCollection, SegmentRelations>()
    val arcs = mutableMapOf<ArcPointCollection, ArcRelations>()
    val circles = mutableMapOf<IdentNotation, CircleRelations>() // IdentNotation is used to rename and remap to work

    val segmentVectors = VectorContainer<SegmentPointCollection>()
    val angleVectors = VectorContainer<AnglePointCollection>()

    val equalIdentRenamer = EqualIdentRenamer()

    fun getRelationsByNotation(notation: Notation): EntityRelations {
        return getKeyValueByNotation(notation).second
    }

    fun getKeyByNotation(notation: Notation): Any = getKeyValueByNotation(notation).first

    @Suppress("UNCHECKED_CAST")
    fun getKeyValueByNotation(notation: Notation): Pair<Any, EntityRelations> {
        when (notation) {
            is PointNotation ->
                return notation.p to getPoint(notation)

            is RayNotation -> return getPair<RayRelations, RayNotation, RayPointCollection>(
                notation,
                rays as LinkedList<MutablePair<PointCollection<RayNotation>, RayRelations>>,
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
            is Point2Notation -> return getPair<LineRelations, Point2Notation, LinePointCollection>(
                notation,
                lines as LinkedList<MutablePair<PointCollection<Point2Notation>, LineRelations>>,
                arrayOf(notation.getPointsAndCircles().toMutableSet())
            )
            is Point3Notation -> return getAngleAndRelations(notation)
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

    private inline fun <reified T : EntityRelations,
            reified N : Notation,
            reified C : PointCollection<N>> getPair(
        notation: N,
        list: LinkedList<MutablePair<PointCollection<N>, T>>,
        constructorArgs: Array<Any>
    ): Pair<PointCollection<N>, T> {
        // have to iterate over all collection, because if we find by key, we can't take the key
        // e.g. can find by RayCollection("A", ("B")), but key is RayCollection("A", ("B", "C"))
        for ((collection, line) in list) {
            if (collection.isFromNotation(notation))
                return collection to line
        }

        val linearRelations = T::class.constructors.first().call()
        val collection = C::class.constructors.first().call(*constructorArgs)
        list.add(collection with linearRelations)
        equalIdentRenamer.addSubscribers(collection, *notation.getPointsAndCircles().toTypedArray())
        if (notation is ArcNotation)
            equalIdentRenamer.addSubscribers(collection, notation.circle)

        return collection to linearRelations
    }

    /**
     * Set newRelations to notation
     * @param notation notation to find what value to set
     */
    fun resetLine(newRelations: LineRelations, notation: Point2Notation) {
        resetLinear(lines, notation, newRelations)
    }

    fun resetSegment(newRelations: SegmentRelations, notation: SegmentNotation) {
        val collection = SegmentPointCollection(mutableSetOf(notation.p1, notation.p2))
        segments[collection] = newRelations
    }

    fun resetRay(newRelations: RayRelations, notation: RayNotation) {
        resetLinear(rays, notation, newRelations)
    }

    private fun <T : PointCollection<*>, R : EntityRelations> resetLinear(
        linearCollection: LinkedList<MutablePair<T, R>>,
        notation: Point2Notation,
        newRelations: R
    ) {
        for (pair in linearCollection) {
            if (pair.e1.getPointsInCollection().containsAll(notation.getPointsAndCircles())) {
                pair.e2 = newRelations
                return
            }
        }
    }

    fun resetPoint(newRelations: PointRelations, notation: String) {
        points[notation] = newRelations
    }

    private fun getAngleAndRelations(notation: Point3Notation): Pair<AnglePointCollection, AngleRelations> {
        for ((collection, relations) in angles) {
            if (collection.isFromNotation(notation))
                return collection to relations
        }
        val newRelations = AngleRelations()
        val collection = getAngleCollectionFromNotation(notation)
        // each AnglePointCollection should be contained in SymbolTable.angles
        angles.add(collection with newRelations)
        return collection to newRelations
    }

    fun resetAngle(newRelations: AngleRelations, notation: Point3Notation) {
        angles.find { it.e1 == getAngleAndRelations(notation).first }!!.e2 = newRelations
    }

    fun getPointSetNotationByNotation(notation: Notation): Set<String> {
        return when (notation) {
            is PointNotation -> setOf(notation.p)
            is Point2Notation -> (getKeyValueByNotation(notation).first as PointCollection<*>).getPointsInCollection()
            is Point3Notation -> setOf(notation.p1, notation.p2, notation.p3)
            is IdentNotation -> getCircle(notation).getPoints()
            else -> throw SpoofError("Unexpected notation: %{notation}", "notation" to notation)
        }
    }

    private fun getAngleCollectionFromNotation(notation: Point3Notation): AnglePointCollection {
        val leftArm = getKeyByNotation(RayNotation(notation.p2, notation.p1)) as RayPointCollection
        val rightArm = getKeyByNotation(RayNotation(notation.p2, notation.p3)) as RayPointCollection
        val res = AnglePointCollection(
            leftArm,
            rightArm
        )
        leftArm.addAngle(res)
        rightArm.addAngle(res)
        return res
    }

    fun getPointObjectsByNotation(notation: Notation): Set<PointRelations> =
        getPointSetNotationByNotation(notation).map { getPoint(it) }.toSet()

    /**
     * Make point distinct from all others
     */
    fun newPoint(point: String): PointRelations {
        if (points[point] != null)
            throw SpoofError("Point %{name} already defined", "name" to point)
        points[point] = PointRelations()
        equalIdentRenamer.addPoint(point)
        return points[point]!!
    }

    fun newCircle(notation: IdentNotation): CircleRelations {
        if (circles[notation] != null)
            throw SpoofError("Circle %{name} already defined", "name" to notation.text)
        circles[notation] = CircleRelations()
        equalIdentRenamer.addPoint(notation.text)
        return circles[notation]!!
    }

    fun hasPoint(pointNotation: PointNotation): Boolean {
        return points[pointNotation.p] != null
    }

    fun hasPoint(point: String): Boolean {
        return points[point] != null
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

        arcToAngleList.clear()
        angleVectors.vectors.clear()
        segmentVectors.vectors.clear()

        equalIdentRenamer.clear()
    }

    fun getOrCreateVector(notation: Notation): Vector {
        when (notation) {
            is NumNotation -> return mutableMapOf(multiSetOf(0) to if (notation.number.isZero()) FractionFactory.one() else notation.number)
            is ArcNotation -> {
                val angle = arcToAngleList.find {
                    it.e1 == ArcPointCollection(
                        notation.getPointsAndCircles().toMutableSet(), circle = notation.circle
                    )
                } ?: throw SpoofError("Angle for arc %{arc} not specified", "arc" to notation)
                return angleVectors.getOrCreate(angle.e2)
            }
            is Point3Notation -> return angleVectors.getOrCreate(getKeyByNotation(notation) as AnglePointCollection)
            is SegmentNotation -> {
                val key = SegmentPointCollection(notation.getPointsAndCircles().toMutableSet())
                if (segmentVectors.vectors[key] == null)
                    equalIdentRenamer.addSubscribers(key, *key.getPointsInCollection().toTypedArray())
                return segmentVectors.getOrCreate(key)
            }
            is MulNotation -> {
                return notation.elements.map {
                    getOrCreateVector(it)
                }.fold(mutableMapOf(multiSetOf<Int>() to FractionFactory.one())) { acc, i ->
                    acc.mergeWith(i, "*")
                }
            }
            else -> throw SpoofError(
                "Unexpected notation %{notation} in arithmetic expression",
                "notation" to notation
            )
        }
    }

    fun assertCorrectState() {
        assert(angleVectors.vectors.size == angleVectors.vectors.keys.toSet().size)
        assert(segmentVectors.vectors.size == segmentVectors.vectors.keys.toSet().size)

        assertMapCorrect(points)
        assertMapCorrect(circles)
        assertMapCorrect(segments)
        assertMapCorrect(arcs)

        assertLinkedListCorrect(angles)
        assertLinkedListCorrect(rays)
        assertLinkedListCorrect(lines)
        assertLinkedListCorrect(arcToAngleList)
    }

    fun <A, B> assertLinkedListCorrect(list: LinkedList<MutablePair<A, B>>) {
        assert(list.size == list.map { it.e1 }.toSet().size)
    }

    private fun <A, B> assertMapCorrect(map: Map<A, B>) {
        assert(map.size == map.keys.toSet().size)
    }
}
