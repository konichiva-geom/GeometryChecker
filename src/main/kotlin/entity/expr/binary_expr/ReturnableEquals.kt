package entity.expr.binary_expr

import entity.expr.Expr
import entity.expr.Returnable
import entity.expr.notation.Notation
import entity.expr.notation.PointNotation
import error.SpoofError
import pipeline.interpreter.IdentMapperInterface
import pipeline.symbol_table.SymbolTable

class ReturnableEquals(left: Expr, right: Expr) : BinaryExpr(left, right) {
    override fun getRepr(): StringBuilder = left.getRepr().append(" == ").append(right.getRepr())

    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface): Expr =
        ReturnableEquals(
            left.createNewWithMappedPointsAndCircles(mapper),
            right.createNewWithMappedPointsAndCircles(mapper)
        )

    override fun check(symbolTable: SymbolTable): Boolean {
        val returnValue = (right as Returnable).getReturnValue(symbolTable)
//        if (left::class != returnValue::class)
//            throw SpoofError("relation is faulty")
        return symbolTable.getRelationsByNotation(left as Notation) ==
                symbolTable.getRelationsByNotation(PointNotation(returnValue.first()))
    }

    override fun make(symbolTable: SymbolTable) {
        val returnValue = (right as Returnable).getReturnValue(symbolTable)
        if (left::class != returnValue::class)
            throw SpoofError("Cannot make a relation because they are of different classes")
    }

    override fun toString(): String = "$left == $right"
}
