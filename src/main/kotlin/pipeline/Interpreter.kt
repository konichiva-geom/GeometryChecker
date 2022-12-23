package pipeline

import SymbolTable
import TheoremParser
import com.github.h0tk3y.betterParse.st.SyntaxTree
import com.github.h0tk3y.betterParse.utils.Tuple2
import expr.BinaryExpr
import expr.Expr
import expr.TheoremUse

class Interpreter {
    private val theoremParser = TheoremParser()
    private val symbolTable = SymbolTable()

    fun interpret(tree: SyntaxTree<List<Tuple2<Any, List<Expr>>>>) {
        theoremParser.addTheorems()
        interpretDescription(tree.item[0].t2)
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