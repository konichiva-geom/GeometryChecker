package inference

import expr.AnyExpr
import expr.Expr

/**
 * Convert result of expressions from inference.txt
 */
open class Inference(
    fromSide: List<Expr>,
    toSide: List<Expr>
) {
    val fromSideExpressions = mutableListOf<Expr>()
    val toSideExpressions = mutableListOf<Expr>()
    val fromSideQuantifier = mutableListOf<AnyExpr>()

    init {
        fromSide.forEach {
            if (it is AnyExpr)
                fromSideQuantifier.add(it)
            else fromSideExpressions.add(it)
        }
        toSideExpressions.addAll(toSide)
    }

    open fun process(newlyAddedExpr: Expr) {
    }
}

class DoubleSidedInference(
    fromSide: List<Expr>,
    toSide: List<Expr>
) : Inference(fromSide, toSide) {
    val toSideQuantifier = mutableListOf<AnyExpr>()

    init {
        toSideQuantifier.addAll(toSideExpressions.filterIsInstance<AnyExpr>())
        toSideExpressions.retainAll { it !is AnyExpr }
    }
}