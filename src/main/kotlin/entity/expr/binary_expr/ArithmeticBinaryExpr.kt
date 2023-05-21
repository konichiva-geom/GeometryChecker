package entity.expr.binary_expr

import entity.expr.Expr
import entity.expr.Returnable
import entity.expr.notation.*
import entity.point_collection.PointCollection
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

    override fun toString(): String = "$left == $right"

    override fun check(symbolTable: SymbolTable): Boolean {
        left as ArithmeticExpr
        right as ArithmeticExpr
        if (!isEntityEquals(left, right))
            throw SpoofError("=== operator can only be used for two notations, not arithmetic expressions")
        val leftNotation = left.map.keys.first()
        if (leftNotation is Point3Notation)
            throw SpoofError("Cannot use this operator for angles. To make points same, use == for points")
        if (leftNotation !is SegmentNotation)
            WarnLogger.warn("Use === only for segments, for other relations use ==")
        return symbolTable.getRelationsByNotation(leftNotation) ==
                symbolTable.getRelationsByNotation(right.map.keys.first())
    }

    override fun make(symbolTable: SymbolTable) {
        left as ArithmeticExpr
        right as ArithmeticExpr
        if (!isEntityEquals(left, right))
            throw SpoofError("=== operator can only be used for two notations, not arithmetic expressions")
        when (val leftNotation = left.map.keys.first()) {
            is PointNotation -> symbolTable.getPoint(leftNotation.p)
                .mergeOtherToThisPoint(leftNotation.p, (right.map.keys.first() as PointNotation).p, symbolTable)

            is IdentNotation -> symbolTable.getCircle(leftNotation)
                .mergeOtherToThisCircle(leftNotation, right.map.keys.first() as IdentNotation, symbolTable)

            else -> symbolTable.getRelationsByNotation(left.map.keys.first()).merge(right.map.keys.first(), symbolTable)
        }
    }
}

class ReturnableEquals(left: Notation, right: Expr) : BinaryExpr(left, right) {
    override fun getRepr(): StringBuilder = left.getRepr().append(" == ").append(right.getRepr())

    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface): Expr = TODO("Not yet implemented")

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

    override fun toString(): String = "$left == $right"
}

class ReturnableNotEquals(left: Notation, right: Expr) : BinaryExpr(left, right) {
    override fun getRepr(): StringBuilder = TODO("Not yet implemented")
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapperInterface): Expr = TODO("Not yet implemented")
    override fun check(symbolTable: SymbolTable): Boolean = TODO("Not yet implemented")
    override fun make(symbolTable: SymbolTable) = TODO("Not yet implemented")
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
        return if (isNotArithmeticEntityEquals(left, right))
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

        if (isNotArithmeticEntityEquals(left, right)) {
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
            val (notation, result) = arithmeticExpressionsToVector(left, right, symbolTable)

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
                    checkTriangleEquation(left, right)
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

private fun isNotArithmeticEntityEquals(left: ArithmeticExpr, right: ArithmeticExpr) =
    isEntityEquals(left, right)
            && left.map.keys.first() !is Point3Notation
            && left.map.keys.first() !is SegmentNotation
            && left.map.keys.first() !is TriangleNotation

private fun arithmeticExpressionsToVector(
    left: ArithmeticExpr,
    right: ArithmeticExpr,
    symbolTable: SymbolTable
): Pair<Notation?, Vector> {
    val resolveLeft = vectorFromArithmeticMap(left.map, symbolTable)
    val resolveRight = vectorFromArithmeticMap(right.map, symbolTable)
    val notation = left.map.mergeWithOperation(right.map, "-")
        .keys.firstOrNull { it !is NumNotation }

    val result = resolveLeft.mergeWithOperation(resolveRight, "-")
    return notation to result
}

private fun checkTriangleEquation(left: ArithmeticExpr, right: ArithmeticExpr) {
    if (left.map.size != 1 || right.map.size != 1
        || left.map.keys.first() !is TriangleNotation
        || right.map.keys.first() !is TriangleNotation
    ) {
        throw SpoofError("Triangle equations shouldn't contain any expressions besides triangles")
    }
}

private fun incorrectNotation(notation: Notation, isAngleCorrect: Boolean = true): Nothing {
    throw SpoofError(
        "Notation %{notation} is not supported in arithmetic expressions, " +
                "use segments, angles${if (isAngleCorrect) " and angles" else ""}",
        "notation" to notation
    )
}

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

        if (isNotArithmeticEntityEquals(left, right)) {
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
        val (notation, vector) = arithmeticExpressionsToVector(left, right, symbolTable)
        val negated = vector.minus()
        if (vector.all { it.value.isAlmostZero() }) {
            throw SpoofError("Expression is incorrect")
        }
        if (notation == null)
            return true
        return when (notation) {
            is SegmentNotation -> symbolTable.segmentVectors.notEqualVectors.contains(vector)
                    || symbolTable.segmentVectors.notEqualVectors.contains(negated)

            is Point3Notation -> symbolTable.angleVectors.notEqualVectors.contains(vector)
                    || symbolTable.angleVectors.notEqualVectors.contains(negated)

            is TriangleNotation -> {
                checkTriangleEquation(left, right)
                (symbolTable.triangleVectors.notEqualVectors.contains(vector)
                        || symbolTable.triangleVectors.notEqualVectors.contains(negated))
            }

            else -> incorrectNotation(notation)
        }
    }

    override fun make(symbolTable: SymbolTable) {
        left as ArithmeticExpr
        right as ArithmeticExpr

        if (isNotArithmeticEntityEquals(left, right)) {
            val leftNotation = left.map.keys.first()
            val rightNotation = right.map.keys.first()
            if (leftNotation is PointNotation && rightNotation is PointNotation) {
                symbolTable.getPoint(leftNotation.p).unknown.remove(rightNotation.p)
                symbolTable.getPoint(rightNotation.p).unknown.remove(leftNotation.p)
                return
            }
        } else {
            val (notation, vector) = arithmeticExpressionsToVector(left, right, symbolTable)

            if (vector.all { it.value.isAlmostZero() }) {
                throw SpoofError("Expression is incorrect")
            }

            if (notation == null || (vector.keys.size == 1 && vector.keys.first() == multiSetOf(0)))
                return

            when (notation) {
                is SegmentNotation -> symbolTable.segmentVectors.addNotEqualVector(vector)
                is Point3Notation -> symbolTable.angleVectors.addNotEqualVector(vector)
                is TriangleNotation -> {
                    checkTriangleEquation(left, right)
                    symbolTable.triangleVectors.addNotEqualVector(vector)
                }

                is MulNotation -> {
                    TODO("Not yet implemented")
                }

                else -> incorrectNotation(notation)
            }
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
        left as ArithmeticExpr
        right as ArithmeticExpr

        val (notation, vector) = arithmeticExpressionsToVector(left, right, symbolTable)

        if (vector.all { it.value.isAlmostZero() }
            || (vector.keys.size == 1 && vector.keys.first() == multiSetOf(0) && vector.values.first() <= 0)) {
            throw SpoofError("Expression is incorrect")
        }
        // vector is majorly bigger than zero
        if (vector.all { it.value >= 0 })
            return true
        if (notation == null)
            return true
        return when (notation) {
            is SegmentNotation -> symbolTable.segmentVectors.isBiggerContained(vector)
            is Point3Notation -> symbolTable.angleVectors.isBiggerContained(vector)
            is MulNotation -> TODO("Not yet implemented")
            else -> incorrectNotation(notation, false)
        }
    }

    override fun make(symbolTable: SymbolTable) {
        left as ArithmeticExpr
        right as ArithmeticExpr

        val (notation, vector) = arithmeticExpressionsToVector(left, right, symbolTable)

        if (vector.all { it.value.isAlmostZero() }
            || (vector.keys.size == 1 && vector.keys.first() == multiSetOf(0) && vector.values.first() <= 0)) {
            throw SpoofError("Expression is incorrect")
        }
        if (notation == null)
            return
        when (notation) {
            is SegmentNotation -> symbolTable.segmentVectors.addBiggerVector(vector)
            is Point3Notation -> symbolTable.angleVectors.addBiggerVector(vector)
            is MulNotation -> TODO("Not yet implemented")
            else -> incorrectNotation(notation, false)
        }
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
        left as ArithmeticExpr
        right as ArithmeticExpr

        val (notation, vector) = arithmeticExpressionsToVector(left, right, symbolTable)

        if (vector.keys.size == 1 && vector.keys.first() == multiSetOf(0) && vector.values.first() <= 0) {
            throw SpoofError("Expression is incorrect")
        }
        // vector is majorly bigger than zero
        if (notation == null || vector.all { it.value.isAlmostZero() || it.value >= 0.0 })
            return true
        return when (notation) {
            is SegmentNotation -> symbolTable.segmentVectors.isBiggerOrEqualContained(vector)
            is Point3Notation -> symbolTable.angleVectors.isBiggerOrEqualContained(vector)
            is MulNotation -> TODO("Not yet implemented")
            else -> incorrectNotation(notation, false)
        }
    }

    override fun make(symbolTable: SymbolTable) {
        left as ArithmeticExpr
        right as ArithmeticExpr

        val (notation, vector) = arithmeticExpressionsToVector(left, right, symbolTable)

        if (vector.keys.size == 1 && vector.keys.first() == multiSetOf(0) && vector.values.first() < 0) {
            throw SpoofError("Expression is incorrect")
        }
        if (notation == null || vector.all { it.value.isAlmostZero() || it.value >= 0.0 })
            return
        when (notation) {
            is SegmentNotation -> symbolTable.segmentVectors.addBiggerOrEqualVector(vector)
            is Point3Notation -> symbolTable.angleVectors.addBiggerOrEqualVector(vector)
            is MulNotation -> TODO("Not yet implemented")
            else -> incorrectNotation(notation, false)
        }
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
