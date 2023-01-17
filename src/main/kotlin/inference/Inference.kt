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
        val searchedRepr = newlyAddedExpr.getRepr().toString()
        toSideExpressions.filter { it.getRepr().toString() == searchedRepr }.forEach {
            mapper.traverseExpr(newlyAddedExpr, it)
        }
        mapper.forceUniqueMappings()
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

    override fun process(newlyAddedExpr: Expr, symbolTable: SymbolTable, mapper: ExpressionMapper) {
        val searchedRepr = newlyAddedExpr.getRepr().toString()
        toSideExpressions.filter { it.getRepr().toString() == searchedRepr }.forEach {
            mapper.traverseExpr(newlyAddedExpr, it)
        }
        mapper.forceUniqueMappings()
        mapper.clearMappings()
    }

    fun processSide(
        newlyAddedExpr: Expr,
        symbolTable: SymbolTable,
        mapper: ExpressionMapper,
        isToSide: Boolean = true
    ) {
        val searchedRepr = newlyAddedExpr.getRepr().toString()
        (if (isToSide) toSideExpressions else fromSideExpressions).filter { it.getRepr().toString() == searchedRepr }
            .forEach {
                mapper.traverseExpr(newlyAddedExpr, it)
            }
        mapper.forceUniqueMappings()
        mapper.clearMappings()
    }
}
