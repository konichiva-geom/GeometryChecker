package pipeline

import PosError
import Relation
import SpoofError
import SymbolTable
import TheoremParser
import com.github.h0tk3y.betterParse.lexer.LiteralToken
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.st.SyntaxTree
import com.github.h0tk3y.betterParse.utils.Tuple2
import expr.Creation
import expr.Expr
import expr.TheoremUse

class Interpreter {
    val theoremParser = TheoremParser()
    private val symbolTable = SymbolTable()

    fun interpret(tree: SyntaxTree<List<Tuple2<Any, List<Expr>>>>) {
        checkHeaders(
            tree.item as List<Tuple2<String, *>>,
            TokenMatch(LiteralToken("", ""), 0, "", 0, tree.range.count(), 0, 0)
        )
        interpretDescription(tree.item[0].t2, tree.children[0].children[1])
        checkIfProven(tree.item[1].t2, tree.children[1].children[1])
    }

    fun checkHeaders(blocks: List<Tuple2<String, *>>, allMatch: TokenMatch) {
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
                    is Relation -> expr.make(symbolTable)
                    is Creation -> expr.create(symbolTable)
                    else -> {
                    }
                }
            } catch (e: SpoofError) {
                throw PosError(syntaxTree.children[i].range, e.msg, *e.args)
            }
        }
    }

    private fun interpretTheoremUse(expr: TheoremUse) {
        if (expr.signature.name == "check") {
            for (rel in expr.signature.args) {
                if (rel !is Relation)
                    throw SpoofError("Cannot check %{expr}, because it is not a relation", "expr" to rel)
                theoremParser.check(rel, symbolTable)
            }
        } else {
            val body = theoremParser.getTheoremBodyBySignature(expr.signature)
            theoremParser.parseTheorem(expr.signature, theoremParser.getSignature(expr.signature), body)
        }
    }

    private fun checkIfProven(block: List<Expr>, syntaxTree: SyntaxTree<*>) {
        for ((i, expr) in block.withIndex()) {
            try {
                when (expr) {
                    is Relation -> theoremParser.check(expr, symbolTable)
                    else -> throw SpoofError("Expected relation to check")
                }
            } catch (e: SpoofError) {
                throw PosError(syntaxTree.children[i].range, e.msg, *e.args)
            }
        }
    }
}
