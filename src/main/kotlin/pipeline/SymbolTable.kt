import Utils.sortAngle
import entity.AngleRelations
import entity.EntityRelations
import entity.LineRelations
import entity.Point
import entity.RayRelations
import entity.SegmentRelations
import expr.BinaryEquals
import expr.BinaryIn
import expr.BinaryIntersects
import expr.BinaryParallel
import expr.Expr
import expr.Notation
import expr.Point2Notation
import expr.Point3Notation
import expr.PointNotation
import expr.RayNotation
import expr.SegmentNotation
import kotlin.reflect.KClass

interface PointCollection {
    fun getPointsInCollection(): Set<String>

    fun getRespectableNotationClass(symbolTable: SymbolTable): KClass<out Notation>
}

data class LinePointCollection(val points: MutableSet<String>) : PointCollection {
    override fun getPointsInCollection(): Set<String> = points
    override fun getRespectableNotationClass(symbolTable: SymbolTable): KClass<out Notation> = Point2Notation::class
}

data class RayPointCollection(val start: String, val points: MutableSet<String>) : PointCollection {
    override fun getPointsInCollection(): Set<String> = setOf(start) + points
    override fun getRespectableNotationClass(symbolTable: SymbolTable): KClass<out Notation> = RayNotation::class
}

data class SegmentPointCollection(val bounds: Set<String>, val points: MutableSet<String>) : PointCollection {
    override fun getPointsInCollection(): Set<String> = bounds + points
    override fun getRespectableNotationClass(symbolTable: SymbolTable): KClass<out Notation> = SegmentNotation::class
}

open class SymbolTable {
    private val points = mutableMapOf<String, Point>()
    val lines = mutableMapOf<LinePointCollection, LineRelations>()
    val rays = mutableMapOf<RayPointCollection, RayRelations>()
    val segments = mutableMapOf<SegmentPointCollection, SegmentRelations>()
    val linear = mapOf(
        RayNotation::class to rays,
        SegmentNotation::class to segments,
        Point2Notation::class to lines
    )
    private val angles = mutableMapOf<Point3Notation, AngleRelations>()
    private val mappings = mutableMapOf<Notation, Vector<Int>>()
    var addRelations = false

    fun getRelationsByNotation(notation: Notation): EntityRelations {
        return when (notation) {
            is PointNotation -> getPoint(notation)
            is RayNotation -> getRay(notation)
            is SegmentNotation -> getSegment(notation)
            is Point2Notation -> getLine(notation)
            is Point3Notation -> getAngle(notation)
            else -> throw SpoofError(notation.toString())
        }
    }

    fun getKeyValueByNotation(notation: Notation): Pair<Any, EntityRelations> {
        when (notation) {
            is PointNotation -> return notation.p to getPoint(notation)
            is Point2Notation -> {
                for ((collection, value) in linear[notation::class]!!)
                    if (collection.getPointsInCollection().containsAll(notation.getLetters()))
                        return collection to value
                throw SpoofError("not found")
            }

            is Point3Notation -> return notation to getAngle(notation)
            else -> throw SpoofError(notation.toString())
        }
    }

    fun resetLine(newRelations: LineRelations, notation: Point2Notation) {
        for ((searchedNotation, _) in lines) {
            if (searchedNotation.getPointsInCollection().containsAll(notation.getLetters())) {
                lines[searchedNotation] = newRelations
                return
            }
        }
    }

    fun getPointSetNotationByNotation(notation: Notation): Set<String> {
        return when (notation) {
            is PointNotation -> setOf(notation.p)
            is Point2Notation -> getKeyValueByNotation(notation).first as Set<String>
            is Point3Notation -> return setOf(notation.p1, notation.p2, notation.p3)
            else -> throw SpoofError(notation.toString())
        }
    }

    /**
     * Make point distinct from all others
     */
    fun newPoint(notation: PointNotation): Point {
        // println(name)
        return Point()
        // if (points[name] != null)
        //     throw Exception("Point ${name} already defined")
        // points[name] = Point(points.keys.toMutableSet())
        // return points[name]!!
    }

    fun getPoint(name: String): Point {
        return points[name] ?: throw Exception("Point $name is not instantiated")
    }

    fun getPoint(pointNotation: PointNotation): Point {
        return getPoint(pointNotation.p)
    }

    fun getLine(notation: Point2Notation): LineRelations {
        return getLinear(
            notation,
            lines as MutableMap<MutableSet<String>, EntityRelations>,
            LineRelations::class
        ) as LineRelations
    }

    private fun getLinear(
        notation: Point2Notation,
        linearStorage: MutableMap<MutableSet<String>, EntityRelations>,
        linearClass: KClass<out EntityRelations>
    ): EntityRelations {
        linearStorage.forEach {
            if (it.key.contains(notation.p1) && it.key.contains(notation.p2))
                return it.value
        }
        val res = linearClass.constructors.first().call()
        linearStorage[mutableSetOf(notation.p1, notation.p2)] = res
        return res
    }

    fun getRay(notation: RayNotation): RayRelations {
        return RayRelations()//return getLinear(notation, segments as MutableMap<MutableSet<String>, Entity>, Line::class) as Line
    }

    fun getSegment(notation: SegmentNotation): SegmentRelations {
        return SegmentRelations()//return getLinear(notation, lines as MutableMap<MutableSet<String>, Entity>, Line::class) as Line
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

    open fun handleRelation() {}
}

class DescriptionTable : SymbolTable()

class SolutionTable : SymbolTable()