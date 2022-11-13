import Utils.sortAngle
import Utils.sortLine
import entity.Angle
import entity.Line
import entity.Point
import notation.Notation
import notation.Point2Notation
import notation.Point3Notation
import notation.PointNotation

open class SymbolTable {
    private val points = mutableMapOf<String, Point>()
    private val lines = mutableMapOf<Point2Notation, Line>()
    private val angles = mutableMapOf<Point3Notation, Angle>()
    private val mappings = mutableMapOf<Notation, Vector<Int>>()

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

    fun getLine(notation: Point2Notation): Line {
        sortLine(notation)
        // if (points[notation.p1] == null || points[notation.p2] == null)
        //     throw Exception("Point ${if (points[notation.p1] == null) notation.p1 else notation.p2} not found")
        if (lines[notation] != null)
            return lines[notation]!!
        lines[notation] = Line()
        return lines[notation]!!
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

class DescriptionTable() : SymbolTable() {

}

class SolutionTable() : SymbolTable() {

}