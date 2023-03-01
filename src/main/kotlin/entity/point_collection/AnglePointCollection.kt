package entity.point_collection

import entity.expr.notation.Point3Notation
import error.SpoofError
import error.SystemFatalError
import pipeline.SymbolTable

class AnglePointCollection(var leftArm: RayPointCollection, var rightArm: RayPointCollection) :
    PointCollection<Point3Notation>() {
    constructor(firstSet: MutableSet<String>, pivot: String, secondSet: MutableSet<String>)
            : this(RayPointCollection(pivot, firstSet), RayPointCollection(pivot, secondSet))

    override fun renameToMinimalAndRemap(symbolTable: SymbolTable) {
        leftArm.renameToMinimalAndRemap(symbolTable)
        rightArm.renameToMinimalAndRemap(symbolTable)
    }

    override fun checkValidityAfterRename(): Exception? {
        if (leftArm.start != rightArm.start)
            return SpoofError("angle collection is invalid after rename")
        return null
    }

    override fun getPointsInCollection(): Set<String> {
        return leftArm.getPointsInCollection() + rightArm.getPointsInCollection()
    }

    override fun isFromNotation(notation: Point3Notation): Boolean {
        val leftPointCollection = leftArm.getPointsInCollection()
        val rightPointCollection = rightArm.getPointsInCollection()
        return notation.p2 == leftArm.start
                && (leftPointCollection.contains(notation.p1) && rightPointCollection.contains(notation.p3)
                || leftPointCollection.contains(notation.p3) && rightPointCollection.contains(notation.p1))
    }

    override fun addPoints(added: List<String>, symbolTable: SymbolTable) {
        throw SystemFatalError("AnglePointCollection.addPoints() shouldn't be called")
    }

    override fun merge(other: PointCollection<*>, symbolTable: SymbolTable) {}

    override fun toString(): String {
        val leftPoints = leftArm.getPointsInCollection().toMutableSet()
        val rightPoints = rightArm.getPointsInCollection().toMutableSet()
        leftPoints.remove(leftArm.start)
        rightPoints.remove(leftArm.start)
        return "${leftPoints.joinToString(separator = " ")}-${leftArm.start}-${rightPoints.joinToString(separator = " ")}"
    }

    override fun equals(other: Any?): Boolean {
        if (other !is AnglePointCollection)
            return false
        return leftArm == other.leftArm && rightArm == other.rightArm
    }

    override fun hashCode(): Int {
        return 31 * leftArm.hashCode() + rightArm.hashCode()
    }
}