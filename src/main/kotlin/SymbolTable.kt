import Utils.sortAngle
import Utils.sortLine
import entity.Angle
import entity.Entity
import entity.Line
import entity.Point
import entity.Ray
import entity.Segment
import expr.Notation
import expr.Point2Notation
import expr.Point3Notation
import expr.PointNotation
import expr.RayNotation
import expr.SegmentNotation

open class SymbolTable {
    private val points = mutableMapOf<String, Point>()
    private val lines = mutableMapOf<Point2Notation, Line>()
    private val rays = mutableMapOf<RayNotation, Ray>()
    private val segments = mutableMapOf<SegmentNotation, Segment>()
    private val angles = mutableMapOf<Point3Notation, Angle>()
    private val mappings = mutableMapOf<Notation, Vector<Int>>()
    var addRelations = false

    fun getByNotation(notation: Notation): Entity {
        return when (notation) {
            is PointNotation -> getPoint(notation)
            is RayNotation -> getRay(notation)
            is SegmentNotation -> getSegment(notation)
            is Point2Notation -> getLine(notation)
            is Point3Notation -> getAngle(notation)
            else -> throw PosError("Cannot get %{} from symbol table", notation)
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

    fun getLine(notation: Point2Notation): Line {
        sortLine(notation)
        // if (points[notation.p1] == null || points[notation.p2] == null)
        //     throw Exception("Point ${if (points[notation.p1] == null) notation.p1 else notation.p2} not found")
        if (lines[notation] != null)
            return lines[notation]!!
        lines[notation] = Line()
        return lines[notation]!!
    }

    fun getRay(notation: RayNotation): Ray {
        if (rays[notation] != null)
            return rays[notation]!!
        rays[notation] = Ray()
        return rays[notation]!!
    }

    fun getSegment(notation: SegmentNotation): Segment {
        sortLine(notation)
        if (segments[notation] != null)
            return segments[notation]!!
        segments[notation] = Segment()
        return segments[notation]!!
    }

    fun getAngle(notation: Point3Notation): Angle {
        sortAngle(notation)
        // if (points[notation.p1] == null || points[notation.p2] == null || points[notation.p3] == null)
        //     throw Exception("Point ${if (points[notation.p1] == null) notation.p1 else if (points[notation.p2] == null) notation.p2 else notation.p3} not found")
        if (angles[notation] != null)
            return angles[notation]!!
        angles[notation] = Angle()
        return angles[notation]!!
    }

    open fun handleRelation() {}
}

class DescriptionTable : SymbolTable()

class SolutionTable : SymbolTable()