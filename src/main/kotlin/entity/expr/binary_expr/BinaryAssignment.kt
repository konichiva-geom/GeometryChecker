package entity.expr.binary_expr

import entity.expr.Creation
import entity.expr.Expr
import entity.expr.Returnable
import entity.expr.notation.Notation
import entity.expr.notation.PointNotation
import error.SpoofError
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
            throw SpoofError("Cannot assign from %{entity.expr}", "entity.expr" to right)
        if (left is PointNotation) {
            if (!symbolTable.hasPoint(left))
                return false
            return symbolTable.getPoint(left) == symbolTable.getPoint(right.getReturnValue(symbolTable) as String)
        } else throw SpoofError("Assigning non-points is not yet implemented")
    }

    override fun make(symbolTable: SymbolTable) {
        if (!(right as BinaryExpr).check(symbolTable))
            throw SpoofError("prove %{entity.expr} first, then assign", "entity.expr" to right)
        if (right !is Returnable)
            throw SpoofError("Cannot assign from %{entity.expr}", "entity.expr" to right)
        if (left is PointNotation) {
            val newRelations = symbolTable.newPoint(left)
            val assignValue = (right as Returnable).getReturnValue(symbolTable)
            if (assignValue.size != 1)
                throw SpoofError("Expected 1 assign point")
            newRelations.mergeOtherToThisPoint(left, PointNotation(assignValue.first()), symbolTable)
        } else throw SpoofError("Assigning non-points is not yet implemented")
    }

    override fun create(symbolTable: SymbolTable) {
        TODO("Not yet implemented")
    }
}
