package math

import entity.expr.Expr
import entity.expr.notation.Notation
import pipeline.ArithmeticExpander.getArithmeticToString
import pipeline.interpreter.IdentMapper

class ArithmeticExpr(val map: MutableMap<Notation, Fraction>) : Expr {
    override fun getChildren(): List<Expr> = map.keys.toList()

    override fun getRepr(): StringBuilder {
        return getArithmeticToString(map)
    }

    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapper): Expr {
        val res = mutableMapOf<Notation, Fraction>()

        for ((notation, fraction) in map)
            res[notation.createNewWithMappedPointsAndCircles(mapper) as Notation] = fraction

        return ArithmeticExpr(res)
    }

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return getArithmeticToString(map).toString()
    }
}