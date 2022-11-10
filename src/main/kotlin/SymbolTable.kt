import Utils.sortAngle
import Utils.sortLine
import entity.Angle
import entity.Line
import entity.Point

open class SymbolTable {
    private val points = mutableMapOf<String, Point>()
    private val lines = mutableMapOf<Pair<String, String>, Line>()
    private val angles = mutableMapOf<Triple<String, String, String>, Angle>()

    /**
     * Make point distinct from all others
     */
    fun newPoint(name: String): Point {
        println(name)
        return Point()
        // if (points[name] != null)
        //     throw Exception("Point ${name} already defined")
        // points[name] = Point(points.keys.toMutableSet())
        // return points[name]!!
    }

    fun getLine(up1: String, up2: String): Line {
        val (p1, p2) = sortLine(up1, up2)
        if (points[p1] == null || points[p2] == null)
            throw Exception("Point ${if (points[p1] == null) p1 else p2} not found")
        val ident = Pair(p1, p2)
        if (lines[ident] != null)
            return lines[ident]!!
        lines[ident] = Line()
        return lines[ident]!!
    }

    fun getAngle(up1: String, up2: String, up3: String): Angle {
        val (p1, p2, p3) = sortAngle(up1, up2, up3)
        if (points[p1] == null || points[p2] == null || points[p3] == null)
            throw Exception("Point ${if (points[p1] == null) p1 else if (points[p2] == null) p2 else p3} not found")
        val ident = Triple(p1, p2, p3)
        if (angles[ident] != null)
            return angles[ident]!!
        angles[ident] = Angle()
        return angles[ident]!!
    }

    open fun handleRelation() {}
}

class DescriptionTable() : SymbolTable() {

}
class SolutionTable() : SymbolTable() {

}