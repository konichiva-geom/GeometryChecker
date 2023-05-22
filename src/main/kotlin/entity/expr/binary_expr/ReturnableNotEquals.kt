package entity.expr.binary_expr

import entity.expr.Expr
import entity.expr.notation.Notation
import pipeline.interpreter.IdentMapperInterface
import pipeline.symbol_table.SymbolTable

class ReturnableNotEquals(left: Notation, right: Expr) : BinaryExpr(left, right) {
    override fun getRepr(): StringBuilder = TODO("Not yet implemented")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface): Expr = TODO("Not yet implemented")
    override fun check(symbolTable: SymbolTable): Boolean = TODO("Not yet implemented")
    override fun make(symbolTable: SymbolTable) = TODO("Not yet implemented")
}
