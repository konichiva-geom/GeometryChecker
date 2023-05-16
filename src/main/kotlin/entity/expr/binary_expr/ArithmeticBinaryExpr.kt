package entity.expr.binary_expr

import entity.expr.Expr
import entity.expr.Returnable
import entity.expr.notation.*
import entity.point_collection.PointCollection
import entity.relation.TriangleRelations
import error.SpoofError
import error.SystemFatalError
import external.WarnLogger
import math.*
import pipeline.interpreter.IdentMapperInterface
import pipeline.symbol_table.SymbolTable
import utils.ExtensionUtils.isAlmostZero
import utils.CommonUtils.isSame
import utils.multiSetOf

class BinarySame(left: Expr, right: Expr) : BinaryExpr(left, right) {
    override fun getRepr() = getReprForBinaryWithExpressions(left, right, " === ")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface) = BinarySame(
        left.createNewWithMappedPointsAndCircles(mapper),
        right.createNewWithMappedPointsAndCircles(mapper)
    )

    override fun toString(): String {
        return "$left == $right"
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        left as ArithmeticExpr
        right as ArithmeticExpr
        if (!isEntityEquals(left, right))
            throw SpoofError("=== operator can only be used for two notations, not arithmetic expressions")
        val leftNotation = left.map.keys.first()
        if (leftNotation is Point3Notation)
            throw SpoofError("Cannot use this operator for angles. To make points same, use == for points")
        if (leftNotation !is Point3Notation && leftNotation !is SegmentNotation)
            WarnLogger.warn("Use === only for segments, for other relations use ==")
        return symbolTable.getRelationsByNotation(leftNotation) ==
                symbolTable.getRelationsByNotation(right.map.keys.first())
    }

    override fun make(symbolTable: SymbolTable) {
        left as ArithmeticExpr
        right as ArithmeticExpr
        if (!isEntityEquals(left, right))
            throw SpoofError("=== operator can only be used for two notations, not arithmetic expressions")
        val leftNotation = left.map.keys.first()
        if (leftNotation is PointNotation)
            symbolTable.getPoint(leftNotation.p)
                .mergeOtherToThisPoint(leftNotation.p, (right.map.keys.first() as PointNotation).p, symbolTable)
        else if (leftNotation is IdentNotation)
            symbolTable.getCircle(leftNotation)
                .mergeOtherToThisCircle(leftNotation, right.map.keys.first() as IdentNotation, symbolTable)
        else {
            symbolTable.getRelationsByNotation(left.map.keys.first()).merge(right.map.keys.first(), symbolTable)
        }
    }
}

class ReturnableEquals(left: Notation, right: Expr) : BinaryExpr(left, right) {
    override fun getRepr(): StringBuilder {
        return left.getRepr().append(" == ").append(right.getRepr())
    }

    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface): Expr {
        TODO("Not yet implemented")
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        val returnValue = (right as Returnable).getReturnValue(symbolTable)
//        if (left::class != returnValue::class)
//            throw SpoofError("relation is faulty")
        return symbolTable.getRelationsByNotation(left as Notation) ==
                symbolTable.getRelationsByNotation(PointNotation(returnValue.first()))
    }

    override fun make(symbolTable: SymbolTable) {
        val returnValue = (right as Returnable).getReturnValue(symbolTable)
        if (left::class != returnValue::class)
            throw SpoofError("Cannot make a relation because they are of different classes")

    }

    override fun toString(): String {
        return "$left == $right"
    }
}

class ReturnableNotEquals(left: Notation, right: Expr) : BinaryExpr(left, right) {
    override fun getRepr(): StringBuilder {
        TODO("Not yet implemented")
    }

    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface): Expr {
        TODO("Not yet implemented")
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        TODO("Not yet implemented")
    }

    override fun make(symbolTable: SymbolTable) {
        TODO("Not yet implemented")
    }

}

class BinaryEquals(left: Expr, right: Expr) : BinaryExpr(left, right) {
    override fun getRepr() = getReprForBinaryWithExpressions(left, right, " == ")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface) = BinaryEquals(
        left.createNewWithMappedPointsAndCircles(mapper),
        right.createNewWithMappedPointsAndCircles(mapper)
    )

    override fun toString(): String {
        return "$left == $right"
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        left as ArithmeticExpr
        right as ArithmeticExpr
        return if (isEntityEquals(left, right)
            && left.map.keys.first() !is Point3Notation
            && left.map.keys.first() !is SegmentNotation
            && left.map.keys.first() !is TriangleNotation
        )
            symbolTable.getRelationsByNotation(left.map.keys.first()) ==
                    symbolTable.getRelationsByNotation(right.map.keys.first())
        else {
            val resolveLeft = vectorFromArithmeticMap(left.map, symbolTable)
            val resolveRight = vectorFromArithmeticMap(right.map, symbolTable)
            val isZeroVector = resolveLeft.mergeWithOperation(resolveRight, "-")
            return isZeroVector.all { it.value.isAlmostZero() }
        }
    }

    override fun make(symbolTable: SymbolTable) {
        left as ArithmeticExpr
        right as ArithmeticExpr

        if (isEntityEquals(left, right)
            && left.map.keys.first() !is Point3Notation
            && left.map.keys.first() !is SegmentNotation
            && left.map.keys.first() !is TriangleNotation
        ) {
            when (val leftNotation = left.map.keys.first()) {
                is PointNotation -> symbolTable.getPoint(leftNotation.p)
                    .mergeOtherToThisPoint(leftNotation.p, (right.map.keys.first() as PointNotation).p, symbolTable)

                is IdentNotation -> symbolTable.getCircle(leftNotation)
                    .mergeOtherToThisCircle(leftNotation, right.map.keys.first() as IdentNotation, symbolTable)

                else -> {
                    val (collectionLeft, relationsLeft) = symbolTable.getKeyValueByNotation(left.map.keys.first())
                    val (collectionRight, relationsRight) = symbolTable.getKeyValueByNotation(right.map.keys.first())
                    if (collectionLeft is PointCollection<*>) {
                        if (!isSame(collectionLeft, collectionRight))
                            collectionLeft.merge(collectionRight as PointCollection<*>, symbolTable)
                    } else {
                        // won't execute currently, but if something new is added will fail
                        if (collectionLeft !is IdentNotation)
                            throw SystemFatalError("Unexpected notation in equals")
                        throw SystemFatalError("Not done yet for circles")
                    }
                    relationsLeft.merge(null, symbolTable, relationsRight)
                }
            }
        } else {
            val resolveLeft = vectorFromArithmeticMap(left.map, symbolTable)
            val resolveRight = vectorFromArithmeticMap(right.map, symbolTable)
            val notation = left.map.mergeWithOperation(right.map, "-")
                .keys.firstOrNull { it !is NumNotation }

            val result = resolveLeft.mergeWithOperation(resolveRight, "-")
            if (result.all { it.value.isAlmostZero() } || notation == null) {
                WarnLogger.warn("Expression %{expr} is already known", "expr" to this)
                return
            }
            if (result.keys.size == 1 && result.keys.first() == multiSetOf(0)) {
                throw SpoofError("Expression is incorrect")
            }
            when (notation) {
                is SegmentNotation -> symbolTable.segmentVectors.resolveVector(result as Vector)
                is Point3Notation -> symbolTable.angleVectors.resolveVector(result as Vector)
                is TriangleNotation -> {
                    if (left.map.size != 1 || right.map.size != 1
                        || left.map.keys.first() !is TriangleNotation
                        || right.map.keys.first() !is TriangleNotation
                    ) {
                        throw SpoofError("Triangle equations shouldn't contain any expressions besides triangles")
                    }
                    symbolTable.triangleVectors.resolveVector(result as Vector)
                }

                is MulNotation -> {
                    if (notation.elements.first() is SegmentNotation) {
                        symbolTable.segmentVectors.resolveVector(result as Vector)
                    } else if (notation.elements.first() is PointNotation) {
                        symbolTable.angleVectors.resolveVector(result as Vector)
                    } else throw SpoofError(
                        "Notation %{notation} is not supported in arithmetic expressions, " +
                                "use segments and angles",
                        "notation" to notation.elements.first()
                    )
                }

                else -> throw SpoofError(
                    "Notation %{notation} is not supported in arithmetic expressions, " +
                            "use segments and angles",
                    "notation" to notation
                )
            }
        }
    }
}

private fun isEntityEquals(left: ArithmeticExpr, right: ArithmeticExpr) =
    left.map.size == 1 && right.map.size == 1
            && left.map.values.first() == 1.0
            && right.map.values.first() == 1.0

class BinaryNotEquals(left: Expr, right: Expr) : BinaryExpr(left, right) {
    override fun getRepr() = getReprForBinaryWithExpressions(left, right, " != ")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface) = BinaryNotEquals(
        left.createNewWithMappedPointsAndCircles(mapper),
        right.createNewWithMappedPointsAndCircles(mapper)
    )

    override fun toString(): String {
        return "$left != $right"
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        left as ArithmeticExpr
        right as ArithmeticExpr

        if (isEntityEquals(left, right)) {
            val leftNotation = left.map.keys.first()
            val rightNotation = right.map.keys.first()

            if (leftNotation is PointNotation && rightNotation is PointNotation) {
                val leftPoint = symbolTable.getPoint(leftNotation)
                val rightPoint = symbolTable.getPoint(rightNotation)

                if (leftPoint === rightPoint
                    || leftPoint.unknown.contains(rightNotation.p)
                    || rightPoint.unknown.contains(leftNotation.p)
                )
                    return false
                return !(leftPoint.unknown.map { symbolTable.getPoint(it) }.toSet().contains(rightPoint)
                        || rightPoint.unknown.map { symbolTable.getPoint(it) }.toSet().contains(leftPoint))
            } else {
                TODO("Not yet implemented")
            }
        }
        TODO("Not yet implemented")
    }

    override fun make(symbolTable: SymbolTable) {
        left as ArithmeticExpr
        right as ArithmeticExpr

        if (isEntityEquals(left, right)) {
            val leftNotation = left.map.keys.first()
            val rightNotation = right.map.keys.first()
            if (leftNotation is PointNotation && rightNotation is PointNotation) {
                symbolTable.getPoint(leftNotation.p).unknown.remove(rightNotation.p)
                symbolTable.getPoint(rightNotation.p).unknown.remove(leftNotation.p)
                return
            }
        } else {
            TODO("Not yet implemented")
        }
    }
}

class BinaryGreater(left: Expr, right: Expr) : BinaryExpr(left, right) {
    override fun getRepr() = getReprForBinaryWithExpressions(left, right, " > ")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface) = BinaryGreater(
        left.createNewWithMappedPointsAndCircles(mapper),
        right.createNewWithMappedPointsAndCircles(mapper)
    )

    override fun toString(): String {
        return "$left > $right"
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        TODO("Not yet implemented")
    }

    override fun make(symbolTable: SymbolTable) {
        TODO("Not yet implemented")
    }
}

class BinaryGEQ(left: Expr, right: Expr) : BinaryExpr(left, right) {
    override fun getRepr() = getReprForBinaryWithExpressions(left, right, " >= ")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface) =
        BinaryGEQ(left.createNewWithMappedPointsAndCircles(mapper), right.createNewWithMappedPointsAndCircles(mapper))

    override fun toString(): String {
        return "$left >= $right"
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        TODO("Not yet implemented")
    }

    override fun make(symbolTable: SymbolTable) {
        TODO("Not yet implemented")
    }
}

/**
 * Sort left and right representations by:
 * 1. Notation
 * 2. Size of repr
 * 3. Hashcode, if nothing above works
 *
 * Not using hashcode for all to make it more predictable
 */
private fun getReprForBinaryWithExpressions(left: Expr, right: Expr, sign: String): StringBuilder {
    if (left is Notation && right is Notation)
        return if (left.getOrder() > right.getOrder())
            right.getRepr().append(sign).append(left.getRepr())
        else left.getRepr().append(sign).append(right.getRepr())
    val leftRepr = left.getRepr()
    val rightRepr = right.getRepr()
    if (leftRepr.length == rightRepr.length) {
        return if (leftRepr.toString().hashCode() > rightRepr.toString().hashCode())
            rightRepr.append(sign).append(leftRepr)
        else leftRepr.append(sign).append(rightRepr)
    }
    return if (leftRepr.length > rightRepr.length)
        right.getRepr().append(sign).append(left.getRepr())
    else left.getRepr().append(sign).append(right.getRepr())
}
