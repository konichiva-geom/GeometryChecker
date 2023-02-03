package math

import entity.expr.Expr
import entity.expr.notation.Notation
import pipeline.ArithmeticExpander.getArithmeticToString
import pipeline.interpreter.IdentMapper

class ArithmeticExpr(val map: MutableMap<Notation, Fraction>) : Expr {
    override fun getChildren(): List<Expr> = map.keys.toList()

    override fun getRepr(): StringBuilder {
        TODO("Not yet implemented")
    }

    override fun mapIdents(mapper: IdentMapper): Expr {
        TODO("Not yet implemented")
    }

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return getArithmeticToString(map)
    }
}