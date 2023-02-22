package pipeline.inference

import entity.expr.AnyExpr
import entity.expr.Expr
import entity.expr.Relation
import entity.expr.notation.Notation
import pipeline.SymbolTable
import pipeline.interpreter.IdentMapper

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
    open fun process(newlyAddedExpr: Expr, symbolTable: SymbolTable, mapper: IdentMapper) {
        val searchedRepr = newlyAddedExpr.getRepr().toString()
        val foundExpr = fromSideExpressions.first { it.getRepr().toString() == searchedRepr }
        mapper.createLinks(newlyAddedExpr, foundExpr)
        mapper.traverseExpr(newlyAddedExpr, foundExpr)

        mapper.forceUniqueMappings()
        // remapping expressions
        val copiedMappings = mapper.mappings.toMap()
        val quantifierVariants = symbolTable.equalIdentRenamer.getAllNSizedPointLists(fromSideQuantifier.size)
        for (variant in quantifierVariants) {
            mapper.mappings.putAll(copiedMappings)
            println(this)
            println(mapper)
            for ((i, point) in variant.withIndex())
                mapper.mappings[fromSideQuantifier[i].notation.toString()] = mutableSetOf(point)

            println(mapper)
            println()
            val mappedToSideExpressions = toSideExpressions.map { it.createNewWithMappedPointsAndCircles(mapper) }
            if (mappedToSideExpressions.any {
                    it.traverseExpr(symbolTable) { expr, _ ->
                        if (expr !is Notation)
                            false
                        else {
                            expr.checkValidityAfterRename() != null
                        }
                    }
                })
                continue
            //mappedToSideExpressions.forEach { (it as Notation).checkValidityAfterRename() }

            mapper.clear()
            for (expr in mappedToSideExpressions) {
                // TODO probably should rename expr. Or not, because all points should be minimal already?
                Relation.makeRelation(expr as Relation, symbolTable, fromInference = true)
            }
        }
    }

    override fun toString(): String {
        return "${fromSideQuantifier.joinToString(separator = ", ")}${
            if (fromSideQuantifier.size > 0) ", " else ""
        }${
            fromSideExpressions.joinToString(", ")
        } => ${toSideExpressions.joinToString(", ")}"
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

    override fun process(newlyAddedExpr: Expr, symbolTable: SymbolTable, mapper: IdentMapper) {
        val searchedRepr = newlyAddedExpr.getRepr().toString()
        toSideExpressions.filter { it.getRepr().toString() == searchedRepr }.forEach {
            mapper.createLinks(newlyAddedExpr, it)
        }

        toSideExpressions.filter { it.getRepr().toString() == searchedRepr }.forEach {
            mapper.traverseExpr(newlyAddedExpr, it)
        }
        mapper.forceUniqueMappings()
        for (expr in toSideExpressions) {
            Relation.makeRelation(expr as Relation, symbolTable)
        }
        mapper.clear()
    }

    fun processSide(
        newlyAddedExpr: Expr,
        symbolTable: SymbolTable,
        mapper: IdentMapper,
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
            Relation.makeRelation(expr as Relation, symbolTable)
        }
        mapper.clear()
    }
}
