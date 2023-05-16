package pipeline.symbol_table

import entity.expr.notation.*
import entity.point_collection.*
import entity.relation.*
import error.SpoofError
import error.SystemFatalError
import utils.MutablePair
import utils.with
import java.util.*

open class PointCollectionSymbolTable : PointSymbolTable() {
    val lines = LinkedList<MutablePair<LinePointCollection, LineRelations>>()
    val rays = LinkedList<MutablePair<RayPointCollection, RayRelations>>()
    val angles = LinkedList<MutablePair<AnglePointCollection, AngleRelations>>()
    val arcToAngleList = LinkedList<MutablePair<ArcPointCollection, AnglePointCollection>>()

    val segments = mutableMapOf<SegmentPointCollection, SegmentRelations>()
    val arcs = mutableMapOf<ArcPointCollection, ArcRelations>()

    @Suppress("UNCHECKED_CAST")
    fun getKeyValueByNotation(notation: Notation): Pair<Any, EntityRelations> {
        when (notation) {
            is PointNotation -> return notation.p to getPoint(notation)
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
            is TriangleNotation -> {
                if (triangles[notation] == null)
                    addTriangle(notation)
                return notation to triangles[notation]!!
            }

            else -> throw SystemFatalError("Notation not supported: ${notation.toString()}")
        }
    }

    fun getRelationsByNotation(notation: Notation): EntityRelations {
        return getKeyValueByNotation(notation).second
    }

    fun getKeyByNotation(notation: Notation): Any = getKeyValueByNotation(notation).first

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

    /**
     * For triangles returns vertices and points inside
     */
    fun getPointSetNotationByNotation(notation: Notation): Set<String> {
        return when (notation) {
            is PointNotation -> setOf(notation.p)
            is Point2Notation -> (getKeyValueByNotation(notation).first as PointCollection<*>).getPointsInCollection()
            is Point3Notation -> setOf(notation.p1, notation.p2, notation.p3)
            is IdentNotation -> getCircle(notation).getCirclePoints()
            is TriangleNotation -> getTriangle(notation).pointsInside + notation.getPointsAndCircles()
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

    fun getLine(notation: Point2Notation): LineRelations {
        return getKeyValueByNotation(notation).second as LineRelations
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

    fun getTriangle(notation: TriangleNotation): TriangleRelations {
        return getKeyValueByNotation(notation).second as TriangleRelations
    }
}