package entity.expr.binary_expr

import com.github.h0tk3y.betterParse.utils.Tuple4
import entity.expr.Expr
import entity.expr.notation.Point2Notation
import entity.relation.LineRelations
import pipeline.symbol_table.SymbolTable
import pipeline.interpreter.IdentMapper

/**
 * `||` relation in tree
 */
class BinaryParallel(left: Point2Notation, right: Point2Notation) : BinaryExpr(left, right) {
    override fun getRepr(): StringBuilder = left.getRepr().append(" parallel ").append(right.getRepr())
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapper) =
        BinaryParallel(
            left.createNewWithMappedPointsAndCircles(mapper) as Point2Notation,
            right.createNewWithMappedPointsAndCircles(mapper) as Point2Notation
        )

    override fun toString(): String {
        return "$left || $right"
    }

    /**
     * Check all notations in [left] parallel set to see if one of them corresponds to the [right] [LineRelations]
     */
    override fun check(symbolTable: SymbolTable): Boolean {
        val (_, lineRelations1, _, lineRelations2) = getLinesAndLineRelations(left, right, symbolTable)
        return lineRelations1.parallel.map { symbolTable.getLine(it) }.contains(lineRelations2)
                || lineRelations2.parallel.map { symbolTable.getLine(it) }.contains(lineRelations1)
    }

    override fun make(symbolTable: SymbolTable) {
        val (line1, lineRelations1, line2, lineRelations2) = getLinesAndLineRelations(left, right, symbolTable)
        if (!lineRelations1.parallel.map { symbolTable.getLine(it) }.contains(lineRelations2))
            lineRelations1.parallel.add(line2)
        if (!lineRelations2.parallel.map { symbolTable.getLine(it) }.contains(lineRelations1))
            lineRelations2.parallel.add(line1)
    }
}

/**
 * `⊥` relation in tree
 */
class BinaryPerpendicular(left: Point2Notation, right: Point2Notation) : BinaryExpr(left, right) {
    override fun getRepr(): StringBuilder = left.getRepr().append(" perpendicular ").append(right.getRepr())
    override fun createNewWithMappedPointsAndCircles(mapper: IdentMapper) =
        BinaryPerpendicular(
            left.createNewWithMappedPointsAndCircles(mapper) as Point2Notation,
            right.createNewWithMappedPointsAndCircles(mapper) as Point2Notation
        )

    override fun toString(): String {
        return "$left ⊥ $right"
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        val (_, lineRelations1, _, lineRelations2) = getLinesAndLineRelations(left, right, symbolTable)
        return lineRelations1.perpendicular.map { symbolTable.getLine(it) }.contains(lineRelations2)
                || lineRelations2.perpendicular.map { symbolTable.getLine(it) }.contains(lineRelations1)
    }

    override fun make(symbolTable: SymbolTable) {
        val (line1, lineRelations1, line2, lineRelations2) = getLinesAndLineRelations(left, right, symbolTable)
        if (!lineRelations1.perpendicular.map { symbolTable.getLine(it) }.contains(lineRelations2))
            lineRelations1.perpendicular.add(line2)
        if (!lineRelations2.perpendicular.map { symbolTable.getLine(it) }.contains(lineRelations1))
            lineRelations2.perpendicular.add(line1)
    }
}

private fun getLinesAndLineRelations(
    first: Expr,
    second: Expr,
    symbolTable: SymbolTable
): Tuple4<Point2Notation, LineRelations, Point2Notation, LineRelations> {
    val line1 = (first as Point2Notation).toLine()
    val line2 = (second as Point2Notation).toLine()
    return Tuple4(
        line1, symbolTable.getLine(line1),
        line2, symbolTable.getLine(line2)
    )
}