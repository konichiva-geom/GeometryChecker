package inference

import expr.Expr
import expr.Notation

/**
 * Convert result of expressions from inference.txt
 */
open class Inference(
    val leftSideExpressions: Set<Expr>,
    val rightSideExpressions: Set<Expr>,
    val leftSideQuantifier: Set<Notation>
) {
    open fun process(newlyAddedExpr: Expr) {

    }
}

class DoubleSidedInference(
    leftSideExpressions: Set<Expr>,
    rightSideExpressions: Set<Expr>,
    leftSideQuantifier: Set<Notation>,
    val rightSideQuantifier: Set<Notation>
) : Inference(leftSideExpressions, rightSideExpressions, leftSideQuantifier) {

}