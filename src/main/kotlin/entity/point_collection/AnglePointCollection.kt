package entity.point_collection

import entity.expr.notation.Point3Notation
import pipeline.SymbolTable

class AnglePointCollection(pivot: String, leftArm: RayPointCollection, rightArm: RayPointCollection): PointCollection<Point3Notation>() {
    override fun renameToMinimalAndRemap(symbolTable: SymbolTable) {
        TODO("Not yet implemented")
    }

    override fun checkValidityAfterRename() {
        TODO("Not yet implemented")
    }

    override fun getPointsInCollection(): Set<String> {
        TODO("Not yet implemented")
    }

    override fun isFromNotation(notation: Point3Notation): Boolean {
        TODO("Not yet implemented")
    }

    override fun addPoints(added: List<String>, symbolTable: SymbolTable) {
        TODO("Not yet implemented")
    }

    override fun merge(other: PointCollection<*>) {
        TODO("Not yet implemented")
    }
}