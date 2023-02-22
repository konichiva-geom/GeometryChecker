package entity.expr.binary_expr

import entity.expr.Creation
import entity.expr.Expr
import entity.expr.Relation
import entity.expr.Returnable
import entity.expr.notation.Notation
import entity.expr.notation.Point2Notation
import entity.expr.notation.PointNotation
import error.SpoofError
import error.SystemFatalError
import pipeline.SymbolTable
import pipeline.interpreter.IdentMapper

class BinaryAssignment(left: Notation, right: Expr) : BinaryExpr(left, right), Creation {
    override fun getChildren(): List<Expr> {
        return listOf(right)
    }

    override fun getRepr(): StringBuilder = left.getRepr().append(" = ").append(right.getRepr())

    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapper): Expr =
        BinaryAssignment(
            left.createNewWithMappedPointsAndCircles(mapper) as Notation,
            right.createNewWithMappedPointsAndCircles(mapper)
        )

    override fun check(symbolTable: SymbolTable): Boolean {
        if (!(right as BinaryExpr).check(symbolTable))
            return false
        if (right !is Returnable)
            throw SpoofError("Cannot assign from %{expr}", "expr" to right)
        if (left is PointNotation) {
            if (!symbolTable.hasPoint(left))
                return false
            return symbolTable.getPoint(left) == symbolTable.getPoint(right.getReturnValue(symbolTable) as String)
        } else throw SpoofError("Assigning non-points is not yet implemented")
    }

    override fun make(symbolTable: SymbolTable) {
        assignFromDescriptionAndTheorem(symbolTable)
    }

    private fun assignFromDescriptionAndTheorem(symbolTable: SymbolTable) {
        if (!(right as Relation).check(symbolTable))
            right.make(symbolTable)
        val assignedPoints = (left as Notation).getPointsAndCircles()
        val renamedPoints = (right as Returnable).getReturnValue(symbolTable)
        if (renamedPoints.size > assignedPoints.size) {
            throw SpoofError(
                "Expected %{expected} points, got %{got}",
                "expected" to renamedPoints.size,
                "got" to assignedPoints.size
            )
        }

        assignedPoints.forEach { if (!symbolTable.hasPoint(it)) symbolTable.newPoint(it) }

        if (right is BinaryIntersects) {
            if (renamedPoints.size != 1 && right.left is Point2Notation && right.right is Point2Notation)
                throw SpoofError("Line intersection should be only one point")

            renamedPoints.toList().forEachIndexed { i, s ->
                symbolTable.getPoint(assignedPoints[i])
                    .mergeOtherToThisPoint(assignedPoints[i], s, symbolTable)
            }
            if (renamedPoints.size != assignedPoints.size) {
                val pointsDiff = assignedPoints.size - renamedPoints.size
                renamedPoints.toList().forEachIndexed { i, s ->
                    symbolTable.getPoint(assignedPoints[i])
                        .mergeOtherToThisPoint(assignedPoints[i], s, symbolTable)
                }
                val pointsAddedNewly = assignedPoints.subList(pointsDiff, assignedPoints.size)
                right.addPointsToCircleOrLinear(symbolTable, right.left as Notation, pointsAddedNewly)
                right.addPointsToCircleOrLinear(symbolTable, right.right as Notation, pointsAddedNewly)
            }
        } else {
            throw SystemFatalError("Implemented only for intersection")
        }
    }

    fun assignFromSolution(symbolTable: SymbolTable) {
        if (!(right as Relation).check(symbolTable))
            throw SpoofError("prove %{expr} first, then assign", "expr" to right)
        val renamedPoints = (right as Returnable).getReturnValue(symbolTable)
        if (renamedPoints.any { it.contains("§") })
            throw SpoofError("Do not know the exact number of points to assign")
        val assignedPoints = (left as Notation).getPointsAndCircles()
        if (assignedPoints.any { symbolTable.hasPoint(it) })
            throw SpoofError("Cannot assign already created point")
        if (renamedPoints.size != assignedPoints.size)
            throw SpoofError("Assigned and actual points sizes are different")

        assignedPoints.forEach { symbolTable.newPoint(it) }
        renamedPoints.toList().forEachIndexed { i, s ->
            symbolTable.getPoint(assignedPoints[i])
                .mergeOtherToThisPoint(assignedPoints[i], s, symbolTable)
        }
    }

    override fun create(symbolTable: SymbolTable) {
        TODO("Not yet implemented")
    }
}
