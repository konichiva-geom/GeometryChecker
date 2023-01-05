package pipeline

import SpoofError
import SymbolTable
import TheoremParser
import com.github.h0tk3y.betterParse.lexer.LiteralToken
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.st.SyntaxTree
import com.github.h0tk3y.betterParse.utils.Tuple2
import expr.BinaryExpr
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
        interpretDescription(tree.item[0].t2)
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

    private fun interpretDescription(block: List<Expr>) {
        for (expr in block) {
            when (expr) {
                is TheoremUse -> {
                    val body = theoremParser.getTheoremBodyBySignature(expr.signature)
                    theoremParser.parseTheorem(expr.signature, theoremParser.getSignature(expr.signature), body)
                }

                is BinaryExpr -> {
                    expr.make(symbolTable)
                }

                else -> {
                }
            }
        }
    }
}