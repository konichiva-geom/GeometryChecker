package inference

import SymbolTable
import expr.AnyExpr
import expr.Expr
import pipeline.interpreter.ExpressionMapper

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

    /**
     * Process inference by first mapping all the letters from the newly added expression
     */
    open fun process(newlyAddedExpr: Expr, symbolTable: SymbolTable, mapper: ExpressionMapper) {
        for (expr in toSideExpressions) {
            if (expr::class == newlyAddedExpr::class) {
                mapper.traverseExpr(newlyAddedExpr, expr)
            }
        }
        mapper.clearMappings()
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