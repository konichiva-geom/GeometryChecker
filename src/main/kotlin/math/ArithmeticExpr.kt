package math

import entity.expr.Expr
import entity.expr.notation.Notation
import pipeline.interpreter.IdentMapper
import utils.Utils

class ArithmeticExpr(val notationFractionMap: MutableMap<Notation, Fraction>) : Expr {
    override fun getChildren(): List<Expr> {
        TODO("Not yet implemented")
    }

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
        return Utils.getArithmeticToString(notationFractionMap)
    }

}