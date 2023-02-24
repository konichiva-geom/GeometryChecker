package entity.point_collection

import entity.expr.notation.Point3Notation
import error.SpoofError
import error.SystemFatalError
import pipeline.SymbolTable

class AnglePointCollection(var pivot: String, val leftArm: RayPointCollection, val rightArm: RayPointCollection) :
    PointCollection<Point3Notation>() {

    override fun renameToMinimalAndRemap(symbolTable: SymbolTable) {
        val vector = getValueFromMap(symbolTable.segmentVectors.vectors, this)

        leftArm.renameToMinimalAndRemap(symbolTable)
        rightArm.renameToMinimalAndRemap(symbolTable)
        pivot = symbolTable.equalIdentRenamer.getIdentical(pivot)

        mergeEntitiesInList(symbolTable.angles, symbolTable)
        addToMap(vector, symbolTable.angleVectors, this)
    }

    override fun checkValidityAfterRename(): Exception? {
        val leftPointCollection = leftArm.getPointsInCollection()
        val rightPointCollection = rightArm.getPointsInCollection()
        if (leftPointCollection.size == 1 && leftPointCollection.contains(pivot)
            || rightPointCollection.size == 1 && rightPointCollection.contains(pivot)
        )
            return SpoofError("angle collection is invalid after rename")
        return null
    }

    override fun getPointsInCollection(): Set<String> {
        return leftArm.getPointsInCollection() + rightArm.getPointsInCollection() + pivot
    }

    override fun isFromNotation(notation: Point3Notation): Boolean {
        val leftPointCollection = leftArm.getPointsInCollection()
        val rightPointCollection = rightArm.getPointsInCollection()
        return notation.p2 == pivot &&
                (leftPointCollection.contains(notation.p1) && rightPointCollection.contains(notation.p3)
                        || leftPointCollection.contains(notation.p3) && rightPointCollection.contains(notation.p1))
    }

    override fun addPoints(added: List<String>, symbolTable: SymbolTable) {
        throw SystemFatalError("AnglePointCollection.addPoints() shouldn't be called")
    }

    override fun merge(other: PointCollection<*>, symbolTable: SymbolTable) {
        other as AnglePointCollection
        assert(pivot == other.pivot)

        if (leftArm == other.leftArm)
            leftArm.merge(other.leftArm, symbolTable)
        if (leftArm == other.rightArm)
            leftArm.merge(other.rightArm, symbolTable)
        if (rightArm == other.leftArm)
            rightArm.merge(other.leftArm, symbolTable)
        if (rightArm == other.rightArm)
            rightArm.merge(other.rightArm, symbolTable)
    }

    override fun toString(): String {
        val leftPoints = leftArm.getPointsInCollection().toMutableSet()
        val rightPoints = rightArm.getPointsInCollection().toMutableSet()
        leftPoints.remove(pivot)
        rightPoints.remove(pivot)
        return "${leftPoints.joinToString(separator = " ")}-$pivot-${rightPoints.joinToString(separator = " ")}"
    }

    override fun equals(other: Any?): Boolean {
        if (other !is AnglePointCollection)
            return false
        return pivot == other.pivot && leftArm == other.leftArm && rightArm == other.rightArm
    }

    override fun hashCode(): Int {
        return pivot.hashCode() + 31 * leftArm.hashCode() + 59 * rightArm.hashCode()
    }
}