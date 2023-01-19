package pipeline.interpreter

import PosError
import Relation
import SpoofError
import SymbolTable
import com.github.h0tk3y.betterParse.lexer.LiteralToken
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.st.SyntaxTree
import com.github.h0tk3y.betterParse.utils.Tuple2
import expr.Creation
import expr.Expr
import expr.TheoremUse
import inference.InferenceProcessor

// TODO interpreter is becoming a pipeline too. Maybe convert it to pipeline and move part of its logic to a separate class
class Interpreter(val inferenceProcessor: InferenceProcessor) {
    val theoremParser = TheoremParser()
    private val symbolTable = SymbolTable()

    fun interpret(tree: SyntaxTree<List<Tuple2<Any, List<Expr>>>>) {
        checkHeaders(
            tree.item as List<Tuple2<String, *>>,
            TokenMatch(LiteralToken("", ""), 0, "", 0, tree.range.count(), 0, 0)
        )
        interpretDescription(tree.item[0].t2, tree.children[0].children[1])
        if (tree.item[2].t2 != null)
            interpretSolution(tree.item[2].t2, tree.children[2].children[1])
        interpretProve(tree.item[1].t2, tree.children[1].children[1])
    }

    private fun checkHeaders(blocks: List<Tuple2<String, *>>, allMatch: TokenMatch) {
        if (blocks[0].t1 != "description"
            || blocks[1].t1 != "prove"
            || blocks[2].t1 != "solution"
        )
            throw SpoofError(
                "Expected structure: %{expected}got: %{got}",
                "expected" to "\ndescription:\n\t...\nprove:\n\t...\nsolution:\n\t...\n",
                "got" to "\n${blocks[0].t1}:\n\t...\n${blocks[1].t1}:\n\t...\n${blocks[2].t1}:\n\t..."
            )
    }

    private fun interpretDescription(block: List<Expr>, syntaxTree: SyntaxTree<*>) {
        for ((i, expr) in block.withIndex()) {
            try {
                when (expr) {
                    is TheoremUse -> interpretTheoremUse(expr)
                    is Relation -> {
                        expr.make(symbolTable)
                        inferenceProcessor.processInference(expr, symbolTable)
                    }

                    is Creation -> expr.create(symbolTable)
                    else -> {
                    }
                }
            } catch (e: SpoofError) {
                throw PosError(syntaxTree.children[i].range, e.msg, *e.args)
            }
        }
    }

    private fun interpretSolution(block: List<Expr>, syntaxTree: SyntaxTree<*>) {
        for ((i, expr) in block.withIndex()) {
          //  try {
                when (expr) {
                    is TheoremUse -> interpretTheoremUse(expr)
                    is Relation -> throw SpoofError("Cannot add relation in solution. Use check to check or theorem to add new relation")
                    is Creation -> expr.create(symbolTable)
                    else -> throw SpoofError("Unexpected expression in solution. Use theorems or creation statements")
                }
           // } catch (e: SpoofError) {
          //      throw PosError(syntaxTree.children[i].range, e.msg, *e.args)
          //  }
        }
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
        for ((i, expr) in block.withIndex()) {
            try {
                when (expr) {
                    is Relation -> theoremParser.check(expr, symbolTable)
                    else -> {
                        if (expr is TheoremUse && expr.signature.name == "check")
                            theoremParser.check(expr.signature.args, symbolTable)
                        else throw SpoofError("Expected relation to check")
                    }
                }
            } catch (e: SpoofError) {
                throw PosError(syntaxTree.children[i].range, e.msg, *e.args)
            }
        }
    }
}
