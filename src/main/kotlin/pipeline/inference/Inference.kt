package pipeline.inference

import entity.expr.*
import entity.expr.notation.IdentNotation
import entity.expr.notation.Notation
import entity.expr.notation.PointNotation
import pipeline.interpreter.IdentMapper
import pipeline.symbol_table.SymbolTable

/**
 * Convert result of expressions from pipeline.inference.txt
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
     * Process pipeline.inference by first mapping all the letters from the newly added expression
     */
    open fun process(
        newlyAddedExpr: Expr,
        symbolTable: SymbolTable,
        mapper: IdentMapper,
        inferenceProcessor: InferenceProcessor
    ) {
        val searchedRepr = newlyAddedExpr.getRepr().toString()
        val foundExpr = fromSideExpressions.first { it.getRepr().toString() == searchedRepr }
        mapper.createLinks(newlyAddedExpr, foundExpr)
        mapper.traverseExpr(newlyAddedExpr, foundExpr)

        mapper.forceUniqueMappings()
        // remapping expressions
        val copiedMappings = mapper.mappings.toMap()
        val quantifierVariants = symbolTable.equalIdentRenamer.getAllNSizedPointLists(fromSideQuantifier.size)
        val allInferencePoints = getAllInferencePoints()
        for (variant in quantifierVariants) {
            mapper.mappings.putAll(copiedMappings)
            for ((i, point) in variant.withIndex())
                mapper.mappings[fromSideQuantifier[i].notation.toString()] = mutableSetOf(point)
            if (mapper.mappings.size < allInferencePoints.size) {
                mapper.clear()
                continue
            }

            val mappedToSideExpressions = toSideExpressions.map { it.createNewWithMappedPointsAndCircles(mapper) }
            if (mappedToSideExpressions.any {
                    it.traverseExpr(symbolTable) { expr, _ ->
                        if (expr !is Notation)
                            false
                        else {
                            expr.checkValidityAfterRename() != null
                        }
                    }
                }) {
                mapper.clear()
                continue
            }
            //mappedToSideExpressions.forEach { (it as Notation).checkValidityAfterRename() }

            mapper.clear()
            for (expr in mappedToSideExpressions) {
                // TODO probably should rename expr. Or not, because all points should be minimal already?
                Relation.makeRelation(expr as Relation, symbolTable, inferenceProcessor, fromInference = true)
                symbolTable.assertCorrectState()
            }
        }
    }

    protected fun getAllInferencePoints(): Set<String> {
        val res = mutableSetOf<String>()
        fromSideQuantifier.forEach { res.addAll(it.notation.getPointsAndCircles()) }
        fromSideExpressions.forEach { expr ->
            res.addAll(expr.getAllChildren()
                .filter { it is PointNotation || it is IdentNotation }
                .map { it.toString() })
        }
        return res
    }

    override fun toString(): String {
        return "${fromSideQuantifier.joinToString(separator = ", ")}${
            if (fromSideQuantifier.size > 0) ", " else ""
        }${
            fromSideExpressions.joinToString(", ")
        } => ${toSideExpressions.joinToString(", ")}"
    }
}

// TODO: make sure it works correctly
class DoubleSidedInference(
    fromSide: List<Expr>,
    toSide: List<Expr>
) : Inference(fromSide, toSide) {
    val toSideQuantifier = mutableListOf<AnyExpr>()

    init {
        toSideQuantifier.addAll(toSideExpressions.filterIsInstance<AnyExpr>())
        toSideExpressions.retainAll { it !is AnyExpr }
    }

    override fun process(
        newlyAddedExpr: Expr,
        symbolTable: SymbolTable,
        mapper: IdentMapper,
        inferenceProcessor: InferenceProcessor
    ) {
        val searchedRepr = newlyAddedExpr.getRepr().toString()
        toSideExpressions.filter { it.getRepr().toString() == searchedRepr }.forEach {
            mapper.createLinks(newlyAddedExpr, it)
        }

        toSideExpressions.filter { it.getRepr().toString() == searchedRepr }.forEach {
            mapper.traverseExpr(newlyAddedExpr, it)
        }
        mapper.forceUniqueMappings()
        for (expr in toSideExpressions) {
            Relation.makeRelation(expr as Relation, symbolTable, inferenceProcessor)
        }
        mapper.clear()
    }

    fun processSide(
        newlyAddedExpr: Expr,
        symbolTable: SymbolTable,
        mapper: IdentMapper,
        inferenceProcessor: InferenceProcessor,
        isToSide: Boolean = true
    ) {
        val searchedRepr = newlyAddedExpr.getRepr().toString()
        (if (isToSide) toSideExpressions else fromSideExpressions).filter { it.getRepr().toString() == searchedRepr }
            .forEach {
                mapper.createLinks(newlyAddedExpr, it)
            }
        (if (isToSide) toSideExpressions else fromSideExpressions).filter { it.getRepr().toString() == searchedRepr }
            .forEach {
                mapper.traverseExpr(newlyAddedExpr, it)
            }
        mapper.forceUniqueMappings()
        for (expr in toSideExpressions) {
            Relation.makeRelation(expr as Relation, symbolTable, inferenceProcessor)
        }
        mapper.clear()
    }
}
