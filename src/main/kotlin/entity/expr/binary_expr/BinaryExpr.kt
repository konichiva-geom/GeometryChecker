package entity.expr.binary_expr

import entity.expr.Expr
import entity.expr.Relation

abstract class BinaryExpr(val left: Expr, val right: Expr) : Expr, Relation {
    override fun getChildren(): List<Expr> = listOf(left, right)

    override fun compareTo(other: Expr): Int {
        TODO("Not yet implemented")
    }

    override fun hashCode(): Int {
        return left.hashCode() * 31 + right.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if(other == null || other::class != this::class)
            return false
        other as BinaryExpr
        return left == other.left && right == other.right
    }
}
