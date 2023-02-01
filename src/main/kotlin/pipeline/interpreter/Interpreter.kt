package pipeline.interpreter

import com.github.h0tk3y.betterParse.st.SyntaxTree
import com.github.h0tk3y.betterParse.utils.Tuple2
import entity.Renamable
import entity.expr.Creation
import entity.expr.Expr
import entity.expr.PointCreation
import entity.expr.TheoremUse
import entity.expr.notation.Point2Notation
import entity.expr.notation.Point3Notation
import entity.expr.notation.PointNotation
import entity.relation.Relation
import error.SpoofError
import pipeline.SymbolTable
import pipeline.inference.InferenceProcessor
import utils.Utils.catchWithRangeAndArgs

class Interpreter(val inferenceProcessor: InferenceProcessor) {
    val theoremParser = TheoremParser()
    private val symbolTable = SymbolTable()
    private var addedRelation = false

    fun interpret(tree: SyntaxTree<List<Tuple2<Any, List<Expr>?>>>) {
        checkHeaders(tree.item as List<Tuple2<String, *>>)
        validatePointInitialization(tree)
        if (tree.item[0].t2 != null)
            interpretDescription(tree.item[0].t2!!, tree.children[0].children[1])
        addedRelation = false
        if (tree.item[2].t2 != null)
            interpretSolution(tree.item[2].t2!!, tree.children[2].children[1])
        if (tree.item[1].t2 != null)
            interpretProve(tree.item[1].t2!!, tree.children[1].children[1])
    }

    private fun validatePointInitialization(tree: SyntaxTree<List<Tuple2<Any, List<Expr>?>>>) {
        val tempTable = SymbolTable()
        for ((i, tuple) in tree.item.withIndex()) {
            if (tuple.t2 == null)
                continue
            for ((j, expr) in tuple.t2!!.withIndex()) {
                catchWithRangeAndArgs({
                    validatePointInitialization(expr, tempTable)
                }, tree.children[i].children[1].children[j].range)
            }
        }
        tempTable.clear()
    }

    private fun rename(expr: Expr) {
        if (expr is Renamable) {
            expr.renameAndRemap(symbolTable)
            expr.checkValidityAfterRename()
        }
        for (child in expr.getChildren())
            rename(child)
    }

    private fun validatePointInitialization(expr: Expr, tempTable: SymbolTable) {
        when (expr) {
            is PointCreation -> expr.create(tempTable)
            is PointNotation -> tempTable.getPoint(expr)
            is Point2Notation -> {
                tempTable.getPoint(expr.p1)
                tempTable.getPoint(expr.p2)
            }

            is Point3Notation -> {
                tempTable.getPoint(expr.p1)
                tempTable.getPoint(expr.p2)
                tempTable.getPoint(expr.p3)
            }
        }
        for (child in expr.getChildren()) {
            validatePointInitialization(child, tempTable)
        }
    }

    private fun checkHeaders(blocks: List<Tuple2<String, *>>) {
        if (blocks[0].t1 != "description"
            || blocks[1].t1 != "prove"
            || blocks[2].t1 != "solution"
        )
            throw SpoofError(
                "Expected structure: %{expected} got: %{got}",
                "expected" to "\ndescription:\n\t...\nprove:\n\t...\nsolution:\n\t...\n",
                "got" to "\n${blocks[0].t1}:\n\t...\n${blocks[1].t1}:\n\t...\n${blocks[2].t1}:\n\t..."
            )
    }

    private fun interpretDescription(block: List<Expr>, syntaxTree: SyntaxTree<*>) {
        for ((i, expr) in block.withIndex())
            catchWithRangeAndArgs({
                if (addedRelation) {
                    addedRelation = false
                    rename(expr)
                }
                when (expr) {
                    is TheoremUse -> {
                        interpretTheoremUse(expr)
                        addedRelation = true
                    }

                    is Relation -> {
                        expr.make(symbolTable)
                        inferenceProcessor.processInference(expr, symbolTable)
                        addedRelation = true
                    }

                    is Creation -> expr.create(symbolTable)
                    else -> throw SpoofError("Unexpected expression in description")
                }
            }, syntaxTree.children[i].range)
    }

    private fun interpretSolution(block: List<Expr>, syntaxTree: SyntaxTree<*>) {
        for ((i, expr) in block.withIndex())
            catchWithRangeAndArgs({
                if (addedRelation) {
                    addedRelation = false
                    rename(expr)
                }
                when (expr) {
                    is TheoremUse -> interpretTheoremUse(expr)
                    is Relation -> throw SpoofError("Cannot add relation in solution. Use check to check or theorem to add new relation")
                    is Creation -> expr.create(symbolTable)
                    else -> throw SpoofError("Unexpected expression in solution. Use theorems or creation statements")
                }
            }, syntaxTree.children[i].range)
    }

    private fun interpretTheoremUse(expr: TheoremUse) {
        if (expr.signature.name == "check") {
            theoremParser.check(expr.signature.args, symbolTable)
        } else {
            val body = theoremParser.getTheoremBodyBySignature(expr.signature)
            theoremParser.parseTheorem(expr.signature, theoremParser.getSignature(expr.signature), body, symbolTable)
        }
    }

    private fun interpretProve(block: List<Expr>, syntaxTree: SyntaxTree<*>) {
        for ((i, expr) in block.withIndex())
            catchWithRangeAndArgs({
                rename(expr)
                when (expr) {
                    is Relation -> theoremParser.check(expr, symbolTable)
                    else -> {
                        if (expr is TheoremUse && expr.signature.name == "check")
                            theoremParser.check(expr.signature.args, symbolTable)
                        else throw SpoofError("Expected relation to check in prove block")
                    }
                }
            }, syntaxTree.children[i].range)
    }
}
