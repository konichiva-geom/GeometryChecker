package pipeline.parser

import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.utils.Tuple3
import entity.expr.Expr
import entity.expr.Returnable
import entity.expr.binary_expr.*
import entity.expr.notation.*
import error.SpoofError
import error.SystemFatalError
import utils.ExtensionUtils.toRange
import utils.Utils

object GrammarHelperMethods {
    internal fun getReturnableEquals(returnableRelation: Expr, notation: Notation, sign: String): BinaryExpr {
        if (returnableRelation !is Returnable)
            throw SpoofError(
                "Cannot compare with %{expr}, because it does not return anything",
                "expr" to returnableRelation
            )
        return when (sign) {
            "==" -> ReturnableEquals(notation, returnableRelation)
            "!=" -> ReturnableNotEquals(notation, returnableRelation)
            else -> throw SpoofError("Expected == or != with returnable relation")
        }
    }

    /**
     * Create relation binary expression
     */
    internal fun getBinaryRelationByString(tuple: Tuple3<Notation, TokenMatch, Notation>): BinaryExpr {
        return Utils.catchWithRangeAndArgs({
            val first = tuple.t1
            val operator = tuple.t2.text
            val second = tuple.t3
            when (operator) {
                "in " -> {
                    checkNotNumber(first, operator)
                    checkNotNumber(second, operator)
                    checkNotCircle(first, operator)
                    checkNotPoint(second, operator)
                    checkNotAngle(first, operator)
                    checkNotAngle(second, operator)
                    if (first is ArcNotation && second !is ArcNotation)
                        throw SpoofError("If arc is at the first position in `in`, then it should be in the second position too")
                    if (second is ArcNotation && first !is PointNotation)
                        throw SpoofError("If arc is at the second position in `in`, then point or arc should be in the first position")
                    checkNoGreaterOrder(first, second)
                    BinaryIn(first, second)
                }
                "intersects ", "∩" -> {
                    checkNotNumber(first, operator)
                    checkNotNumber(second, operator)
                    checkNotPoint(first, operator)
                    checkNotPoint(second, operator)
                    checkNotAngle(first, operator)
                    checkNotAngle(second, operator)
                    BinaryIntersects(first, second)
                }

                "parallel ", "||" -> {
                    checkLinear(first, second, operator)
                    BinaryParallel(first as Point2Notation, second as Point2Notation)
                }

                "perpendicular ", "⊥" -> {
                    checkLinear(first, second, operator)
                    BinaryPerpendicular(first as Point2Notation, second as Point2Notation)
                }

                else -> throw SystemFatalError("Unknown comparison")
            }
        }, tuple.t2.toRange()) as BinaryExpr
    }

    private fun checkNoGreaterOrder(first: Notation, second: Notation) {
        if (first.getOrder() > second.getOrder())
            throw SpoofError("`$first` is 'smaller' than `$second`")
    }

    private fun checkNotNumber(notation: Notation, operator: String) {
        if (notation is NumNotation
        )
            throw SpoofError("`$notation` is number, `$operator` is not applicable to numbers")
    }

    private fun checkNotPoint(notation: Notation, operator: String) {
        if (notation is PointNotation)
            throw SpoofError("`$notation` is point, `$operator` is not applicable to points in this position")
    }

    private fun checkNotAngle(notation: Notation, operator: String) {
        if (notation is Point3Notation)
            throw SpoofError("`$notation` is angle, `$operator` is not applicable to angle in this position")
    }

    private fun checkNotCircle(notation: Notation, operator: String) {
        if (notation is IdentNotation)
            throw SpoofError("`$notation` is circle, `$operator` is not applicable to circle in this position")
    }

    private fun checkLinear(first: Notation, second: Notation, operator: String) {
        if (first !is Point2Notation || second !is Point2Notation || first is ArcNotation || second is ArcNotation)
            throw SpoofError(
                "`${if (first !is Point2Notation) first else second}` is not linear, `$operator` is not applicable"
            )
    }
}