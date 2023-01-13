package pipeline

import PosError
import SpoofError
import Utils.THEOREMS_PATH
import com.github.h0tk3y.betterParse.st.SyntaxTree
import com.github.h0tk3y.betterParse.utils.Tuple2
import expr.Expr
import java.io.File

/**
 * Builder-like class that
 */
class Pipeline {
    val parser = Parser()
    val interpreter = Interpreter()
    lateinit var tree: SyntaxTree<Any>

    // TODO: delete in production
    lateinit var code: String

    fun clearTheorems(): Pipeline {
        interpreter.theoremParser.clearTheorems()
        return this
    }

    fun addTheorems(theorems: String): Pipeline {
        interpreter.theoremParser.addTheorems(theorems)
        return this
    }

    fun addTheoremsFromFile(path: String = THEOREMS_PATH): Pipeline {
        addTheorems(File(path).readText())
        return this
    }

    fun parse(code: String): Pipeline {
        tree = parser.parse(code)
        this.code = code
        return this
    }

    fun parseFile(path: String): Pipeline {
        tree = parser.parse(File(path).readText())
        return this
    }

    fun interpretForProduction(): Pipeline {
        if (!this::tree.isInitialized)
            throw SpoofError("Parse code before interpreting")
        interpreter.interpret(tree as SyntaxTree<List<Tuple2<Any, List<Expr>>>>)
        return this
    }

    fun interpret(): Pipeline {
        if (!this::tree.isInitialized)
            throw SpoofError("Parse code before interpreting")
        try {
            interpreter.interpret(tree as SyntaxTree<List<Tuple2<Any, List<Expr>>>>)
        } catch (e: PosError) {
            throw PosError(e.range, e.msg + "\n${code.substring(e.range)}\n", *e.args)
        }
        return this
    }
}